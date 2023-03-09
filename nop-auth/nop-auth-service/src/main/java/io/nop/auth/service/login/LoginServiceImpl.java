/**
 * Copyright (c) 2017-2023 Nop Platform. All rights reserved.
 * Author: canonical_entropy@163.com
 * Blog:   https://www.zhihu.com/people/canonical-entropy
 * Gitee:  https://gitee.com/canonical-entropy/nop-chaos
 * Github: https://github.com/entropy-cloud/nop-chaos
 */
package io.nop.auth.service.login;

import io.nop.api.core.audit.AuditRequest;
import io.nop.api.core.audit.IAuditService;
import io.nop.api.core.auth.IUserContext;
import io.nop.api.core.config.AppConfig;
import io.nop.api.core.context.ContextProvider;
import io.nop.api.core.convert.ConvertHelper;
import io.nop.api.core.exceptions.ErrorCode;
import io.nop.api.core.exceptions.NopException;
import io.nop.api.core.json.JSON;
import io.nop.api.core.time.CoreMetrics;
import io.nop.api.core.util.FutureHelper;
import io.nop.auth.api.AuthApiConstants;
import io.nop.auth.api.messages.LoginRequest;
import io.nop.auth.api.messages.LogoutRequest;
import io.nop.auth.core.login.AbstractLoginService;
import io.nop.auth.core.login.AuthToken;
import io.nop.auth.core.login.IAuthTokenProvider;
import io.nop.auth.core.login.SessionInfo;
import io.nop.auth.core.login.UserContextImpl;
import io.nop.auth.core.password.IPasswordEncoder;
import io.nop.auth.core.verifycode.IVerifyCodeGenerator;
import io.nop.auth.core.verifycode.VerifyCode;
import io.nop.auth.dao.entity.NopAuthRole;
import io.nop.auth.dao.entity.NopAuthUser;
import io.nop.commons.util.DateHelper;
import io.nop.commons.util.StringHelper;
import io.nop.core.i18n.I18nMessageManager;
import io.nop.dao.DaoConstants;
import io.nop.dao.api.IDaoProvider;
import io.nop.dao.api.IEntityDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletionStage;

import static io.nop.auth.api.AuthApiConstants.LOGIN_TYPE_EMAIL_PASSWORD;
import static io.nop.auth.api.AuthApiConstants.LOGIN_TYPE_PHONE_PASSWORD;
import static io.nop.auth.api.AuthApiConstants.LOGIN_TYPE_USERNAME_PASSWORD;
import static io.nop.auth.api.AuthApiErrors.ARG_LOGIN_TYPE;
import static io.nop.auth.api.AuthApiErrors.ARG_PRINCIPAL_ID;
import static io.nop.auth.service.NopAuthConfigs.CFG_AUTH_ACCESS_TOKEN_EXPIRE_SECONDS;
import static io.nop.auth.service.NopAuthConfigs.CFG_AUTH_ALLOW_CREATE_DEFAULT_USER;
import static io.nop.auth.service.NopAuthConfigs.CFG_AUTH_MAX_LOGIN_FAIL_COUNT;
import static io.nop.auth.service.NopAuthConfigs.CFG_AUTH_REFRESH_TOKEN_EXPIRE_SECONDS;
import static io.nop.auth.service.NopAuthConfigs.CFG_AUTH_VERIFY_CODE_ENABLED;
import static io.nop.auth.service.NopAuthErrors.ERR_AUTH_INVALID_LOGIN_REQUEST;
import static io.nop.auth.service.NopAuthErrors.ERR_AUTH_INVALID_VERIFY_CODE;
import static io.nop.auth.service.NopAuthErrors.ERR_AUTH_LOGIN_CHECK_FAIL;
import static io.nop.auth.service.NopAuthErrors.ERR_AUTH_LOGIN_CHECK_FAIL_TOO_MANY_TIMES;
import static io.nop.auth.service.NopAuthErrors.ERR_AUTH_LOGIN_WITH_UNKNOWN_USER;
import static io.nop.auth.service.NopAuthErrors.ERR_AUTH_USER_NOT_ALLOW_LOGIN;
import static io.nop.commons.util.StringHelper.isYes;

public class LoginServiceImpl extends AbstractLoginService {
    static final Logger LOG = LoggerFactory.getLogger(LoginServiceImpl.class);

    @Inject
    protected IPasswordEncoder passwordEncoder;

    @Inject
    protected IDaoProvider daoProvider;

    @Inject
    protected IAuditService auditService;

    @Inject
    protected IVerifyCodeGenerator verifyCodeGenerator;

    private Set<String> allowedLoginMethods;

    @Inject
    protected IAuthTokenProvider authTokenProvider;

    public Set<String> getAllowedLoginMethods() {
        return allowedLoginMethods;
    }

    public void setAllowedLoginMethods(Set<String> allowedLoginMethods) {
        this.allowedLoginMethods = allowedLoginMethods;
    }

    public void lazyInit() {
        addDefaultUser();
    }

    void addDefaultUser() {
        if (CFG_AUTH_ALLOW_CREATE_DEFAULT_USER.get()) {
            IEntityDao<NopAuthUser> dao = daoProvider.daoFor(NopAuthUser.class);
            // 用户表为空，插入缺省用户
            if (dao.isEmpty()) {
                NopAuthUser user = new NopAuthUser();
                user.setUserName("nop");
                String salt = passwordEncoder.generateSalt();
                user.setPassword(passwordEncoder.encodePassword(salt, "123"));
                user.setSalt(salt);
                user.setOpenId("0");
                user.setNickName("Nopper");
                user.setStatus(AuthApiConstants.USER_STATUS_ACTIVE);
                user.setGender(AuthApiConstants.USER_GENDER_DEFAULT);
                user.setUserType(AuthApiConstants.USER_TYPE_DEFAULT);
                user.setDelFlag(DaoConstants.NO_VALUE);
                user.setCreatedBy("sys");
                user.setUpdatedBy("sys");
                user.setUserId(StringHelper.generateUUID());
                dao.saveEntity(user);
            }
        }
    }

    @Override
    public CompletionStage<IUserContext> loginAsync(LoginRequest request, Map<String, Object> headers) {
        long beginTime = CoreMetrics.currentTimeMillis();

        ErrorCode errorCode = null;
        NopAuthUser user = null;
        int failCount = 0;

        if (CFG_AUTH_VERIFY_CODE_ENABLED.get() && !checkVerifyCode(request)) {
            errorCode = ERR_AUTH_INVALID_VERIFY_CODE;
        } else if (!isValid(request)) {
            errorCode = ERR_AUTH_INVALID_LOGIN_REQUEST;
        } else {
            user = getAuthUser(request);
            if (user == null) {
                errorCode = ERR_AUTH_LOGIN_WITH_UNKNOWN_USER;
            } else {
                failCount = userContextCache.getLoginFailCountForUser(user.getUserName());

                int maxFailCount = CFG_AUTH_MAX_LOGIN_FAIL_COUNT.get();
                if (maxFailCount > 0) {
                    if (failCount >= maxFailCount) {
                        errorCode = ERR_AUTH_LOGIN_CHECK_FAIL_TOO_MANY_TIMES;
                    } else if (!isAllowLogin(user)) {
                        errorCode = ERR_AUTH_USER_NOT_ALLOW_LOGIN;
                    } else if (needCheckPassword(request) && !passwordMatches(user, request)) {
                        errorCode = ERR_AUTH_LOGIN_CHECK_FAIL;
                    }
                }
            }
        }

        if (errorCode != null) {
            failCount++;
            if (user != null)
                userContextCache.setLoginFailCountForUser(user.getUserName(), failCount);

            if (userContextHook != null) {
                userContextHook.onLoginFail(request, errorCode, user == null ? null : user.getUserName(), failCount);
            }

            if (errorCode != ERR_AUTH_INVALID_VERIFY_CODE) {
                auditLogFail(errorCode.getErrorCode(), errorCode.getDescription(), request,
                        user == null ? null : user.getUserName(), beginTime, failCount);
            }

            // 用户名错误对外也只显示用户名或者密码错误
            if (errorCode == ERR_AUTH_LOGIN_WITH_UNKNOWN_USER)
                errorCode = ERR_AUTH_LOGIN_CHECK_FAIL;

            NopException err = new NopException(errorCode).param(ARG_LOGIN_TYPE, request.getLoginType())
                    .param(ARG_PRINCIPAL_ID, request.getPrincipalId());

            return FutureHelper.reject(err);
        } else {
            userContextCache.resetLoginFailCountForUser(user.getUserName());
            UserContextImpl userContext = buildUserContext(user, request);
            autoLogout(userContext);
            saveSession(userContext, request, headers);

            if (userContextHook != null)
                userContextHook.onLoginSuccess(userContext, request);

            return userContextCache.saveUserContextAsync(userContext).thenApply(v -> userContext);
        }
    }

    private boolean isAllowLogin(NopAuthUser user) {
        if (user.getStatus() != AuthApiConstants.USER_STATUS_ACTIVE)
            return false;

        if (user.getExpireAt() != null) {
            long time = DateHelper.localDateTimeToTimestamp(user.getExpireAt()).getTime();
            return time >= CoreMetrics.currentTimeMillis();
        }
        return true;
    }

    protected UserContextImpl buildUserContext(NopAuthUser user, LoginRequest request) {
        UserContextImpl context = new UserContextImpl();
        context.setUserId(user.getUserId());
        context.setOpenId(user.getOpenId());
        context.setUserName(user.getUserName());
        context.setNickName(user.getNickName());
        context.setLocale(request.getLocale() == null ? AppConfig.appLocale() : request.getLocale());
        context.setTimeZone(request.getTimeZone() == null ? AppConfig.appTimezone() : request.getTimeZone());
        context.setDeptId(user.getDeptId());
        context.setTenantId(ContextProvider.currentTenantId());

        Set<NopAuthRole> roles = user.getRoles();
        Set<String> roleIds = new TreeSet<>();
        for (NopAuthRole role : roles) {
            roleIds.add(role.getRoleId());
            if (!StringHelper.isEmpty(role.getChildRoleIds()))
                roleIds.addAll(ConvertHelper.toCsvSet(role.getChildRoleIds()));
        }
        context.setRoles(roleIds);
        if (request.getPrimaryRoleId() != null && roleIds.contains(request.getPrimaryRoleId())) {
            context.setPrimaryRole(request.getPrimaryRoleId());
        } else {
            String roleId = roles.stream().filter(r -> isYes(r.getIsPrimary())).findFirst().map(NopAuthRole::getRoleId)
                    .orElse(null);
            context.setPrimaryRole(roleId);
        }

        return context;
    }

    protected void saveSession(UserContextImpl userContext, LoginRequest request,
                               Map<String, Object> headers) {
        String sessionId = loginSessionStore.saveSession(userContext, request, headers);
        userContext.setSessionId(sessionId);

        userContext.setAccessToken(authTokenProvider.generateAccessToken(userContext, CFG_AUTH_ACCESS_TOKEN_EXPIRE_SECONDS.get()));
        userContext.setRefreshToken(authTokenProvider.generateRefreshToken(userContext, CFG_AUTH_REFRESH_TOKEN_EXPIRE_SECONDS.get()));
    }

    /**
     * 自动退出当前用户的其他活动session
     */
    protected void autoLogout(IUserContext userContext) {
        if (loginSessionStore == null)
            return;

        List<String> sessionIds = loginSessionStore.getActionSessions(userContext.getUserName());

        for (String sessionId : sessionIds) {
            doLogout(AuthApiConstants.LOGOUT_TYPE_RELOGIN, new SessionInfo(userContext.getUserName(), sessionId));
        }
    }

    boolean isValid(LoginRequest request) {
        if (!isValidLoginMethod(request)) {
            LOG.info("nop.auth.invalid-login-method:{}", request.getLoginType());
            return false;
        }

        return !StringHelper.isEmpty(request.getPrincipalId());
    }

    boolean checkVerifyCode(LoginRequest request) {
        String cachedCode = userContextCache.getVerifyCode(request.getVerifySecret());
        boolean b;
        if (cachedCode != null) {
            b = verifyCodeGenerator.checkValid(cachedCode, request.getVerifyCode(), request.getVerifySecret());
        } else {
            b = false;
        }
        if (!b) {
            LOG.debug("nop.auth.verify-code-mismatch:code={},cached={}", request.getVerifyCode(), cachedCode);
        }
        return b;
    }

    NopAuthUser getAuthUser(LoginRequest request) {
        int loginType = request.getLoginType();
        switch (loginType) {
            case LOGIN_TYPE_USERNAME_PASSWORD: {
                return getUserByUserName(request.getPrincipalId());
            }
            case LOGIN_TYPE_EMAIL_PASSWORD: {
                return getUserByEmail(request.getPrincipalId());
            }
            case LOGIN_TYPE_PHONE_PASSWORD: {
                return getUserByPhone(request.getPrincipalId());
            }
            default:
                return null;
        }
    }

    boolean needCheckPassword(LoginRequest request) {
        switch (request.getLoginType()) {
            case LOGIN_TYPE_USERNAME_PASSWORD:
            case LOGIN_TYPE_EMAIL_PASSWORD:
            case LOGIN_TYPE_PHONE_PASSWORD:
                return true;
        }
        return false;
    }

    boolean isValidLoginMethod(LoginRequest request) {
        int loginType = request.getLoginType();
        if (allowedLoginMethods == null || allowedLoginMethods.isEmpty()) {
            return LOGIN_TYPE_USERNAME_PASSWORD == loginType;
        }

        return allowedLoginMethods.contains(String.valueOf(request.getLoginType()));
    }

    protected NopAuthUser getUserByUserName(String userName) {
        NopAuthUser example = new NopAuthUser();
        example.setUserName(userName);
        return daoProvider.daoFor(NopAuthUser.class).findFirstByExample(example);
    }

    private boolean passwordMatches(NopAuthUser user, LoginRequest request) {
        return passwordEncoder.passwordMatches(user.getSalt(), request.getPrincipalSecret(), user.getPassword());
    }

    protected NopAuthUser getUserByPhone(String phone) {
        NopAuthUser example = new NopAuthUser();
        example.setPhone(phone);
        return daoProvider.daoFor(NopAuthUser.class).findFirstByExample(example);
    }

    protected NopAuthUser getUserByEmail(String email) {
        NopAuthUser example = new NopAuthUser();
        example.setEmail(email);
        return daoProvider.daoFor(NopAuthUser.class).findFirstByExample(example);
    }

    protected NopAuthUser getUserByUserId(String userId) {
        return daoProvider.daoFor(NopAuthUser.class).getEntityById(userId);
    }

    protected NopAuthUser getUserByOpenId(String openId, String clientId) {
        NopAuthUser example = new NopAuthUser();
        example.setOpenId(openId);
        return daoProvider.daoFor(NopAuthUser.class).findFirstByExample(example);
    }

    protected void auditLogFail(String errorCode, String defaultMessage, LoginRequest request, String userName,
                                long beginTime, int failCount) {
        AuditRequest audit = new AuditRequest();
        audit.setErrorCode(errorCode);
        String message = I18nMessageManager.instance().getMessage(AppConfig.defaultLocale(), errorCode, defaultMessage);
        audit.setMessage(message);
        audit.setResultStatus(400);
        audit.setActionTime(new Timestamp(CoreMetrics.currentTimeMillis()));
        audit.setAction("login");
        audit.setBizObj("LoginApi");

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("loginType", request.getLoginType());
        map.put("principalId", request.getPrincipalId());
        audit.setRequestData(JSON.stringify(map));

        if (failCount > 1) {
            Map<String, Object> result = new HashMap<>();
            result.put("failCount", failCount);
            audit.setRequestData(JSON.stringify(result));
        }

        if (userName == null)
            userName = "-";
        audit.setUserName(userName);
        audit.setUsedTime(CoreMetrics.currentTimeMillis() - beginTime);
        auditService.saveAudit(audit);
    }

    @Override
    public CompletionStage<Void> logoutAsync(int logoutType, LogoutRequest request) {
        AuthToken authToken = parseAuthToken(request.getAccessToken());
        if (authToken == null)
            return FutureHelper.success(null);

        return doLogout(logoutType, new SessionInfo(authToken.getUserName(), authToken.getSessionId()));
    }

    @Override
    public String generateVerifyCode(String verifySecret) {
        VerifyCode verifyCode = verifyCodeGenerator.generateCode(verifySecret);
        LOG.debug("nop.login.generate-verify-code:{}", verifyCode.getCode());
        userContextCache.setVerifyCode(verifySecret, verifyCode.getCode());
        return verifyCode.getCaptcha();
    }

    @Override
    public AuthToken parseAuthToken(String accessToken) {
        return authTokenProvider.parseAuthToken(accessToken);
    }

    @Override
    public String refreshToken(IUserContext userContext, AuthToken authToken) {
        return authTokenProvider.generateAccessToken(userContext, authToken.getExpireSeconds());
    }
}
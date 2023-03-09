/**
 * Copyright (c) 2017-2023 Nop Platform. All rights reserved.
 * Author: canonical_entropy@163.com
 * Blog:   https://www.zhihu.com/people/canonical-entropy
 * Gitee:  https://gitee.com/canonical-entropy/nop-chaos
 * Github: https://github.com/entropy-cloud/nop-chaos
 */
package io.nop.api.core.exceptions;

import static io.nop.api.core.ApiErrors.ERR_BREAK;

public class NopBreakException extends NopSingletonException {

    public static NopBreakException INSTANCE = new NopBreakException();

    public NopBreakException() {
        super(ERR_BREAK);
    }
}
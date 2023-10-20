/**
 * Copyright (c) 2017-2023 Nop Platform. All rights reserved.
 * Author: canonical_entropy@163.com
 * Blog:   https://www.zhihu.com/people/canonical-entropy
 * Gitee:  https://gitee.com/canonical-entropy/nop-chaos
 * Github: https://github.com/entropy-cloud/nop-chaos
 */
package io.nop.xlang.expr;

import io.nop.api.core.util.SourceLocation;
import io.nop.xlang.ast.Expression;

public interface IExpressionParser {
    Expression parseTemplateExpr(SourceLocation loc, String source, boolean singleExpr, ExprPhase phase);

    Expression parseExpr(SourceLocation loc, String source);
}

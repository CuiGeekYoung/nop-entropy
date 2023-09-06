package io.nop.rule.core.execute;

import io.nop.api.core.exceptions.NopException;
import io.nop.core.lang.eval.IEvalAction;
import io.nop.rule.core.IExecutableRule;
import io.nop.rule.core.IRuleRuntime;

public class DecoratedExecutableRule implements IExecutableRule {
    private final IEvalAction beforeExecute;
    private final IExecutableRule rule;
    private final IEvalAction afterExecute;

    public DecoratedExecutableRule(IEvalAction beforeExecute,
                                   IExecutableRule rule,
                                   IEvalAction afterExecute) {
        this.beforeExecute = beforeExecute;
        this.rule = rule;
        this.afterExecute = afterExecute;
    }

    @Override
    public boolean execute(IRuleRuntime ruleRt) {
        try {
            if (beforeExecute != null)
                beforeExecute.invoke(ruleRt);
            boolean b = rule.execute(ruleRt);
            ruleRt.setRuleMatch(b);
            return b;
        } catch (Exception e) {
            ruleRt.setException(e);
            if (afterExecute != null) {
                afterExecute.invoke(ruleRt);
            }
            throw NopException.adapt(e);
        }
    }
}

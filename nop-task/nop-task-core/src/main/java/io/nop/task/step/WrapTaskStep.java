/**
 * Copyright (c) 2017-2023 Nop Platform. All rights reserved.
 * Author: canonical_entropy@163.com
 * Blog:   https://www.zhihu.com/people/canonical-entropy
 * Gitee:  https://gitee.com/canonical-entropy/nop-chaos
 * Github: https://github.com/entropy-cloud/nop-chaos
 */
package io.nop.task.step;

import io.nop.task.ITaskContext;
import io.nop.task.ITaskStep;
import io.nop.task.ITaskStepState;
import io.nop.task.TaskStepResult;

public class WrapTaskStep extends AbstractTaskStep {
    private ITaskStep step;

    public WrapTaskStep() {
    }

    public WrapTaskStep(ITaskStep step) {
        this.step = step;
    }

    public ITaskStep getStep() {
        return step;
    }

    public void setStep(ITaskStep step) {
        this.step = step;
    }

    @Override
    protected TaskStepResult doExecute(ITaskStepState state, ITaskContext context) {
        return step.execute(state.getRunId(), state, context);
    }
}

/**
 * Copyright (c) 2017-2023 Nop Platform. All rights reserved.
 * Author: canonical_entropy@163.com
 * Blog:   https://www.zhihu.com/people/canonical-entropy
 * Gitee:  https://gitee.com/canonical-entropy/nop-chaos
 * Github: https://github.com/entropy-cloud/nop-chaos
 */
package io.nop.rpc.api.flowcontrol;

import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

public interface IFlowControlRunner {

    <T> CompletionStage<T> runAsync(FlowControlEntry entry,
                                    Supplier<CompletionStage<T>> task);

    <T> T run(FlowControlEntry entry, Supplier<T> task);
}
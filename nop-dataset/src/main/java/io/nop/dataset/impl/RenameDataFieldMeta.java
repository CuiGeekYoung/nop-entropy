/**
 * Copyright (c) 2017-2023 Nop Platform. All rights reserved.
 * Author: canonical_entropy@163.com
 * Blog:   https://www.zhihu.com/people/canonical-entropy
 * Gitee:  https://gitee.com/canonical-entropy/nop-chaos
 * Github: https://github.com/entropy-cloud/nop-chaos
 */
package io.nop.dataset.impl;

import io.nop.commons.type.StdDataType;
import io.nop.dataset.IDataFieldMeta;

public class RenameDataFieldMeta implements IDataFieldMeta {
    private final String name;

    private final IDataFieldMeta fieldMeta;

    public RenameDataFieldMeta(String name, IDataFieldMeta fieldMeta) {
        this.name = name;
        this.fieldMeta = fieldMeta;
    }

    @Override
    public String getFieldName() {
        return name;
    }

    @Override
    public String getSourceFieldName() {
        return fieldMeta.getSourceFieldName();
    }

    @Override
    public String getFieldOwnerEntityName() {
        return fieldMeta.getFieldOwnerEntityName();
    }

    @Override
    public StdDataType getFieldStdType() {
        return fieldMeta.getFieldStdType();
    }

    @Override
    public boolean isComputed() {
        return fieldMeta.isComputed();
    }
}
/**
 * Copyright (c) 2017-2023 Nop Platform. All rights reserved.
 * Author: canonical_entropy@163.com
 * Blog:   https://www.zhihu.com/people/canonical-entropy
 * Gitee:  https://gitee.com/canonical-entropy/nop-chaos
 * Github: https://github.com/entropy-cloud/nop-chaos
 */
package io.nop.batch.core.loader;

import io.nop.api.core.exceptions.NopException;
import io.nop.batch.core.IBatchAggregator;
import io.nop.batch.core.IBatchChunkContext;
import io.nop.batch.core.IBatchChunkListener;
import io.nop.batch.core.IBatchLoader;
import io.nop.batch.core.IBatchRecordFilter;
import io.nop.batch.core.IBatchTaskContext;
import io.nop.batch.core.IBatchTaskListener;
import io.nop.batch.core.common.AbstractBatchResourceHandler;
import io.nop.commons.util.IoHelper;
import io.nop.core.resource.IResource;
import io.nop.core.resource.record.IResourceRecordIO;
import io.nop.dataset.record.IRecordInput;
import io.nop.dataset.record.IRowNumberRecord;
import io.nop.dataset.record.impl.RowNumberRecordInput;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static io.nop.batch.core.BatchErrors.ARG_ITEM_COUNT;
import static io.nop.batch.core.BatchErrors.ARG_READ_COUNT;
import static io.nop.batch.core.BatchErrors.ARG_RESOURCE_PATH;
import static io.nop.batch.core.BatchErrors.ERR_BATCH_TOO_MANY_PROCESSING_ITEMS;

/**
 * 读取数据文件。支持设置aggregator，在读取的过程中计算一些汇总信息
 *
 * @param <S> 数据文件中的记录类型
 * @param <C> BatchChunkContext
 */
public class ResourceRecordLoader<S, C> extends AbstractBatchResourceHandler
        implements IBatchLoader<S, C>, IBatchTaskListener, IBatchChunkListener {
    static final String VAR_PROCESSED_ROW_NUMBER = "processedRowNumber";

    private IResourceRecordIO<S> recordIO;
    private String encoding;

    // 对读取的数据进行汇总处理，例如统计读入的总行数等，最后在complete时写入到数据库中
    private IBatchAggregator<S, Object, ?> aggregator;

    private IBatchRecordFilter<S> filter;

    private IRecordInput<S> input;

    /**
     * 最多读取多少行数据（包含跳过的记录）
     */
    private long maxCount;

    /**
     * 跳过起始的多少行数据
     */
    private long skipCount;

    private int maxProcessingItems = 10000;

    /**
     * 是否确保返回的记录实现{@link IRowNumberRecord}接口，并设置rowNumber为当前读取记录条目数，从1开始
     */
    private boolean recordRowNumber = false;

    /**
     * 是否记录处理状态。如果是，则打开文件的时候会检查此前保存的处理条目数，跳过相应的数据行
     */
    private boolean saveState;

    /**
     * 从行号映射到对应记录的处理状况。false表示正在处理，true表示处理完毕
     */
    private TreeMap<Long, Boolean> processingItems;

    private Object combinedValue;

    private IBatchTaskContext taskContext;

    public int getMaxProcessingItems() {
        return maxProcessingItems;
    }

    public void setMaxProcessingItems(int maxProcessingItems) {
        this.maxProcessingItems = maxProcessingItems;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setAggregator(IBatchAggregator<S, Object, ?> aggregator) {
        this.aggregator = aggregator;
    }

    public void setFilter(IBatchRecordFilter<S> filter) {
        this.filter = filter;
    }

    public IResourceRecordIO<S> getRecordIO() {
        return recordIO;
    }

    public void setRecordIO(IResourceRecordIO<S> recordIO) {
        this.recordIO = recordIO;
    }

    public void setSaveState(boolean saveState) {
        this.saveState = saveState;
    }

    public boolean isSaveState() {
        return saveState;
    }

    public long getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(long maxCount) {
        this.maxCount = maxCount;
    }

    public long getSkipCount() {
        return skipCount;
    }

    public void setSkipCount(long skipCount) {
        this.skipCount = skipCount;
    }

    public boolean isRecordRowNumber() {
        return recordRowNumber;
    }

    public void setRecordRowNumber(boolean recordRowNumber) {
        this.recordRowNumber = recordRowNumber;
    }

    @Override
    public synchronized void onTaskBegin(IBatchTaskContext context) {
        taskContext = context;
        IResource resource = getResource();
        input = recordIO.openInput(resource, encoding);

        long skipCount = getSkipCount(context);

        if (aggregator != null)
            combinedValue = aggregator.createCombinedValue(input.getHeaderMeta(), context);

        if (skipCount > 0) {
            skip(input, skipCount, context);
        }

        if (maxCount > 0) {
            input = input.limit(maxCount);
        }

        if (recordRowNumber) {
            input = new RowNumberRecordInput<>(input);
        }

        if (saveState)
            processingItems = new TreeMap<>();

    }

    private long getSkipCount(IBatchTaskContext context) {
        Long processedRowNumber = getPersistLong(context, VAR_PROCESSED_ROW_NUMBER);

        long skipCount = this.skipCount;
        if (processedRowNumber != null && processedRowNumber > skipCount) {
            skipCount = processedRowNumber;
        }
        return skipCount;
    }

    private void skip(IRecordInput<S> input, long skipCount, IBatchTaskContext context) {
        if (aggregator != null) {
            // 如果设置了aggregator，则需要从头开始遍历所有记录，否则断点重提的时候结果可能不正确。
            for (long i = 0; i < skipCount; i++) {
                if (!input.hasNext())
                    break;
                S item = input.next();
                if (filter != null && filter.accept(item, context))
                    continue;

                aggregator.aggregate(input.next(), combinedValue);
            }
        } else {
            input.skip(skipCount);
        }
    }

    @Override
    public synchronized void onTaskEnd(Throwable exception, IBatchTaskContext context) {
        taskContext = null;
        try {
            if (aggregator != null) {
                aggregator.complete(input.getTrailerMeta(), combinedValue);
                combinedValue = null;
            }
            processingItems = null;
        } finally {
            if (input != null) {
                IoHelper.safeCloseObject(input);
                input = null;
            }
        }
    }

    @Override
    public synchronized void onChunkEnd(Throwable exception, IBatchChunkContext context) {
        if (saveState) {
            // 多个chunk有可能被并行处理，所以可能会乱序完成
            if (context.getChunkItems() != null) {
                for (Object item : context.getChunkItems()) {
                    long rowNumber = getRowNumber(item);
                    if (rowNumber > 0) {
                        processingItems.put(rowNumber, true);
                    }
                }
            }

            // 如果最小的rowNumber已经完成，则记录处理历史
            Iterator<Map.Entry<Long, Boolean>> it = processingItems.entrySet().iterator();
            long completedRow = -1L;
            while (it.hasNext()) {
                Map.Entry<Long, Boolean> entry = it.next();
                if (entry.getValue()) {
                    it.remove();
                    completedRow = entry.getKey();
                } else {
                    break;
                }
            }

            // 如果处理阶段异常，则不会保存到状态变量中，这样下次处理的时候仍然会处理到这些记录
            if (completedRow > 0 && exception != null) {
                setPersistVar(context.getTaskContext(), VAR_PROCESSED_ROW_NUMBER, completedRow);
            }
        }
    }

    @Override
    public synchronized List<S> load(int batchSize, C context) {
        List<S> items = loadItems(batchSize, context);
        if (saveState) {
            for (S item : items) {
                long rowNumber = getRowNumber(item);
                if (rowNumber > 0)
                    processingItems.put(rowNumber, false);
            }
            if (processingItems.size() > maxProcessingItems)
                throw new NopException(ERR_BATCH_TOO_MANY_PROCESSING_ITEMS)
                        .param(ARG_ITEM_COUNT, processingItems.size()).param(ARG_READ_COUNT, input.getReadCount())
                        .param(ARG_RESOURCE_PATH, getResourcePath());
        }

        if (aggregator != null) {
            for (S item : items) {
                aggregator.aggregate(item, combinedValue);
            }
        }
        return items;
    }

    private List<S> loadItems(int batchSize, C ctx) {
        if (filter == null)
            return input.readBatch(batchSize);

        return input.readFiltered(batchSize, item -> filter.accept(item, taskContext));
    }

    private long getRowNumber(Object item) {
        if (item instanceof IRowNumberRecord) {
            return ((IRowNumberRecord) item).getRecordRowNumber();
        }
        return -1L;
    }
}
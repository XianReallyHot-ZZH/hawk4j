package com.yy.hawk4j.core.executor.support.adpter;

import com.yy.hawk4j.common.toolkit.ReflectUtil;
import com.yy.hawk4j.core.executor.DynamicThreadPoolExecutor;

import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * @类描述：（内置）ExecutorServiceTtlWrapper对象的适配器，可以从ExecutorTtlWrapper对象中获得其持有的DynamicThreadPoolExecutor对象
 */
public class TransmittableThreadLocalExecutorServiceAdapter implements DynamicThreadPoolAdapter {

    private static String MATCH_CLASS_NAME = "ExecutorServiceTtlWrapper";

    private static String FIELD_NAME = "executorService";

    @Override
    public boolean match(Object executor) {
        return Objects.equals(MATCH_CLASS_NAME, executor.getClass().getSimpleName());
    }

    @Override
    public DynamicThreadPoolExecutor unwrap(Object executor) {
        Object unwrap = ReflectUtil.getFieldValue(executor, FIELD_NAME);
        if (unwrap != null && unwrap instanceof DynamicThreadPoolExecutor) {
            return (DynamicThreadPoolExecutor) unwrap;
        }
        return null;
    }

    @Override
    public void replace(Object executor, Executor dynamicThreadPoolExecutor) {
        ReflectUtil.setFieldValue(executor, FIELD_NAME, dynamicThreadPoolExecutor);
    }
}

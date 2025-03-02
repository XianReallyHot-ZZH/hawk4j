package com.yy.hawk4j.core.executor.support.adpter;

import com.yy.hawk4j.common.toolkit.ReflectUtil;
import com.yy.hawk4j.core.executor.DynamicThreadPoolExecutor;

import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * @类描述：（内置）ExecutorTtlWrapper对象的适配器，可以从ExecutorTtlWrapper对象中获得其持有的DynamicThreadPoolExecutor对象
 */
public class TransmittableThreadLocalExecutorAdapter implements DynamicThreadPoolAdapter {

    private static String MATCH_CLASS_NAME = "ExecutorTtlWrapper";

    private static String FIELD_NAME = "executor";

    /**
     * 判断传进来的对象是否和当前适配器器对象匹配
     * @param executor objects where there may be instances
     *                 of dynamic thread pools
     * @return
     */
    @Override
    public boolean match(Object executor) {
        return Objects.equals(MATCH_CLASS_NAME, executor.getClass().getSimpleName());
    }

    /**
     * 从ExecutorTtlWrapper对象中获得其持有的DynamicThreadPoolExecutor对象
     * @param executor objects where there may be instances
     *                 of dynamic thread pools
     * @return
     */
    @Override
    public DynamicThreadPoolExecutor unwrap(Object executor) {
        Object unwrap = ReflectUtil.getFieldValue(executor, FIELD_NAME);
        if (unwrap != null && unwrap instanceof DynamicThreadPoolExecutor) {
            return (DynamicThreadPoolExecutor) unwrap;
        }
        return null;
    }

    /**
     * 将dynamicThreadPoolExecutor对象替换到executor中
     * @param executor                  objects where there may be instances
     *                                  of dynamic thread pools
     * @param dynamicThreadPoolExecutor dynamic thread-pool executor
     */
    @Override
    public void replace(Object executor, Executor dynamicThreadPoolExecutor) {
        ReflectUtil.setFieldValue(executor, FIELD_NAME, dynamicThreadPoolExecutor);
    }
}

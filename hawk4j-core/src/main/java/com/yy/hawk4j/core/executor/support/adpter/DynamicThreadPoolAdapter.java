package com.yy.hawk4j.core.executor.support.adpter;

import com.yy.hawk4j.core.executor.DynamicThreadPoolExecutor;

import java.util.concurrent.Executor;

/**
 * Dynamic thread pool adapter. 动态线程池适配器
 * 当动态线程池是某个对象的内部成员时，需要使用适配器来判断、获取、替换该对象内部的动态性线程池
 */
public interface DynamicThreadPoolAdapter {

    /**
     * Check if the object contains thread pool information.
     *
     * @param executor objects where there may be instances
     *                 of dynamic thread pools
     * @return matching results
     */
    boolean match(Object executor);

    /**
     * Get the dynamic thread pool reference in the object.
     *
     * @param executor objects where there may be instances
     *                 of dynamic thread pools
     * @return get the real dynamic thread pool instance
     */
    DynamicThreadPoolExecutor unwrap(Object executor);

    /**
     * If the {@link DynamicThreadPoolAdapter#match(Object)} conditions are met,
     * the thread pool is replaced with a dynamic thread pool.
     *
     * @param executor                  objects where there may be instances
     *                                  of dynamic thread pools
     * @param dynamicThreadPoolExecutor dynamic thread-pool executor
     */
    void replace(Object executor, Executor dynamicThreadPoolExecutor);

}

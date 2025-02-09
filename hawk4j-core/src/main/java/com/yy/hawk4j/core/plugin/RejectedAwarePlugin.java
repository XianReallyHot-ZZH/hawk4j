package com.yy.hawk4j.core.plugin;

import com.yy.hawk4j.core.executor.ExtensibleThreadPoolExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池拒绝任务时会调用的插件
 */
public interface RejectedAwarePlugin extends ThreadPoolPlugin {

    /**
     * Callback before task is rejected.
     * <P>
     * 插件调用时机 {@link ExtensibleThreadPoolExecutor.RejectedAwareHandlerWrapper#rejectedExecution(Runnable, ThreadPoolExecutor)}
     *
     * @param runnable task
     * @param executor executor
     */
    default void beforeRejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {

    }

}

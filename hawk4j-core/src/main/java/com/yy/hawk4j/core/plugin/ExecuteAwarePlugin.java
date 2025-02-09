package com.yy.hawk4j.core.plugin;

import com.yy.hawk4j.core.executor.ExtensibleThreadPoolExecutor;

/**
 * 线程池内的线程调度到任务后，执行任务前后会调用的插件
 */
public interface ExecuteAwarePlugin extends ThreadPoolPlugin {

    /**
     * Callback before task execution.
     *
     * @param thread   thread of executing task
     * @param runnable task
     * @see ExtensibleThreadPoolExecutor#beforeExecute
     */
    default void beforeExecute(Thread thread, Runnable runnable) {
    }

    /**
     * Callback after task execution.
     *
     * @param runnable  runnable
     * @param throwable exception thrown during execution
     * @see ExtensibleThreadPoolExecutor#afterExecute
     */
    default void afterExecute(Runnable runnable, Throwable throwable) {
    }

}

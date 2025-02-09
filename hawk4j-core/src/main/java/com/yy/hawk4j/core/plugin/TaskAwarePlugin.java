package com.yy.hawk4j.core.plugin;

import com.yy.hawk4j.core.executor.ExtensibleThreadPoolExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 将任务提交到线程池时，会被调用的插件
 */
public interface TaskAwarePlugin extends ThreadPoolPlugin {


    /**
     * Callback when task is execute.
     *
     * @param runnable runnable
     * @return tasks to be execute
     * @see ExtensibleThreadPoolExecutor#execute(Runnable)
     */
    default Runnable beforeTaskExecute(Runnable runnable) {
        return runnable;
    }

    /**
     * Callback during the {@link java.util.concurrent.RunnableFuture} task create in thread-pool.
     *
     * @param executor executor
     * @param runnable original task
     * @return Tasks that really need to be performed
     * @see ExtensibleThreadPoolExecutor#newTaskFor(Runnable, Object)
     */
    default <T> Runnable beforeTaskCreate(ThreadPoolExecutor executor, Runnable runnable, T value) {
        return runnable;
    }

    /**
     * Callback during the {@link java.util.concurrent.RunnableFuture} task create in thread-pool.
     *
     * @param executor executor
     * @param callable   original task
     * @return Tasks that really need to be performed
     * @see ExtensibleThreadPoolExecutor#newTaskFor(Callable)
     */
    default <T> Callable<T> beforeTaskCreate(ThreadPoolExecutor executor, Callable<T> callable) {
        return callable;
    }



}

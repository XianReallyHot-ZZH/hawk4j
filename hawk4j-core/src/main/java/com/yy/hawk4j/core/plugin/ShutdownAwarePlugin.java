package com.yy.hawk4j.core.plugin;

import com.yy.hawk4j.core.executor.ExtensibleThreadPoolExecutor;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 在销毁线程池时会被调用的插件
 */
public interface ShutdownAwarePlugin extends ThreadPoolPlugin {

    /**
     * Callback before pool shutdown.
     *
     * @param executor executor
     * @see ExtensibleThreadPoolExecutor#shutdown()
     * @see ExtensibleThreadPoolExecutor#shutdownNow()
     */
    default void beforeShutdown(ThreadPoolExecutor executor) {
    }

    /**
     * Callback after pool shutdown.
     *
     * @param executor       executor
     * @param remainingTasks remainingTasks, or empty if no tasks left or {@link ExtensibleThreadPoolExecutor#shutdown()} called
     * @see ExtensibleThreadPoolExecutor#shutdown()
     * @see ExtensibleThreadPoolExecutor#shutdownNow()
     */
    default void afterShutdown(ThreadPoolExecutor executor, List<Runnable> remainingTasks) {
    }

    /**
     * Callback after pool terminated.
     *
     * @param executor executor
     * @see ExtensibleThreadPoolExecutor#terminated()
     */
    default void afterTerminated(ThreadPoolExecutor executor) {
    }


}

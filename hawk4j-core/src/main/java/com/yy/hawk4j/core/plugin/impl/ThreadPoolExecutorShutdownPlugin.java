package com.yy.hawk4j.core.plugin.impl;

import com.yy.hawk4j.common.toolkit.CollectionUtil;
import com.yy.hawk4j.core.executor.ExtensibleThreadPoolExecutor;
import com.yy.hawk4j.core.plugin.PluginRuntime;
import com.yy.hawk4j.core.plugin.ShutdownAwarePlugin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.*;

/**
 * @方法描述：这个就是线程池停止工作时的扩展插件，线程池停止工作时会调用shutdown方法，在执行shutdown或者shutdownNow方法之前和之后，都可以执行这个插件对象中的方法
 */
@Accessors(chain = true)
@Getter
@Slf4j
@AllArgsConstructor
public class ThreadPoolExecutorShutdownPlugin implements ShutdownAwarePlugin {

    public static final String PLUGIN_NAME = "thread-pool-executor-shutdown-plugin";

    /**
     * Await termination millis
     */
    @Setter
    public long awaitTerminationMillis;

    /**
     * Get id.
     *
     * @return id
     */
    @Override
    public String getId() {
        return PLUGIN_NAME;
    }

    /**
     * Callback before pool shutdown.
     *
     * @param executor executor
     */
    @Override
    public void beforeShutdown(ThreadPoolExecutor executor) {
        if (executor instanceof ExtensibleThreadPoolExecutor) {
            ExtensibleThreadPoolExecutor dynamicThreadPoolExecutor = (ExtensibleThreadPoolExecutor) executor;
            String threadPoolId = dynamicThreadPoolExecutor.getThreadPoolId();
            if (log.isInfoEnabled()) {
                log.info("Before shutting down ExecutorService {}", threadPoolId);
            }
        }
    }

    /**
     * Callback after pool shutdown. <br />
     * cancel the remaining tasks,
     * then wait for pool to terminate according {@link #awaitTerminationMillis} if necessary.
     *
     * @param executor       executor
     * @param remainingTasks remainingTasks
     */
    @Override
    public void afterShutdown(ThreadPoolExecutor executor, List<Runnable> remainingTasks) {
        if (executor instanceof ExtensibleThreadPoolExecutor) {
            ExtensibleThreadPoolExecutor pool = (ExtensibleThreadPoolExecutor) executor;
            if (CollectionUtil.isNotEmpty(remainingTasks)) {
                remainingTasks.forEach(this::cancelRemainingTask);
            }
            awaitTerminationIfNecessary(pool);
        }
    }

    /**
     * Get plugin runtime info.
     *
     * @return plugin runtime info
     */
    @Override
    public PluginRuntime getPluginRuntime() {
        return new PluginRuntime(getId())
                .addInfo("awaitTerminationMillis", awaitTerminationMillis);
    }

    /**
     * Cancel the given remaining task which never commended execution,
     * as returned from {@link ExecutorService#shutdownNow()}.
     *
     * @param task the task to cancel (typically a {@link RunnableFuture})
     * @see RunnableFuture#cancel(boolean)
     * @since 5.0.5
     */
    protected void cancelRemainingTask(Runnable task) {
        if (task instanceof Future) {
            ((Future<?>) task).cancel(true);
        }
    }

    /**
     * Wait for the executor to terminate, according to the value of {@link #awaitTerminationMillis}.
     */
    private void awaitTerminationIfNecessary(ExtensibleThreadPoolExecutor executor) {
        String threadPoolId = executor.getThreadPoolId();
        if (this.awaitTerminationMillis <= 0) {
            return;
        }
        try {
            boolean isTerminated = executor.awaitTermination(this.awaitTerminationMillis, TimeUnit.MILLISECONDS);
            if (!isTerminated && log.isWarnEnabled()) {
                log.warn("Timed out while waiting for executor {} to terminate.", threadPoolId);
            } else {
                log.info("ExecutorService {} has been shutdowned.", threadPoolId);
            }
        } catch (InterruptedException ex) {
            if (log.isWarnEnabled()) {
                log.warn("Interrupted while waiting for executor {} to terminate.", threadPoolId);
            }
            Thread.currentThread().interrupt();
        }
    }



}

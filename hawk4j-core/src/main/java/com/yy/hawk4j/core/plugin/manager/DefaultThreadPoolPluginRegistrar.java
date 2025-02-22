package com.yy.hawk4j.core.plugin.manager;

import com.yy.hawk4j.core.plugin.impl.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @方法描述：默认的线程池插件注册器，通过这个注册器对象，把所有内置的线程池插件都注册到线程池的插件管理器中
 */
@NoArgsConstructor
@AllArgsConstructor
public class DefaultThreadPoolPluginRegistrar implements ThreadPoolPluginRegistrar {

    /**
     * Execute time out
     */
    private long executeTimeOut;

    /**
     * Await termination millis
     */
    private long awaitTerminationMillis;

    /**
     * Create and register plugin for the specified thread-pool instance.
     * 将内置的线程池插件注册到线程池中
     *
     * @param support thread pool plugin manager delegate
     */
    @Override
    public void doRegister(ThreadPoolPluginSupport support) {
        support.register(new TaskDecoratorPlugin());
        support.register(new TaskTimeoutNotifyAlarmPlugin(support.getThreadPoolId(), executeTimeOut, support.getThreadPoolExecutor()));
        support.register(new TaskRejectCountRecordPlugin());
        support.register(new TaskRejectNotifyAlarmPlugin());
        support.register(new TaskTimeRecordPlugin());
        support.register(new ThreadPoolExecutorShutdownPlugin(awaitTerminationMillis));
    }
}

package com.yy.hawk4j.core.plugin.manager;

/**
 * @方法描述：默认的线程池插件注册器，通过这个注册器对象，把线程池的所有插件都注册到线程池的插件管理器中
 */
public class DefaultThreadPoolPluginRegistrar implements ThreadPoolPluginRegistrar {


    /**
     * Create and register plugin for the specified thread-pool instance.
     * 将内置的线程池插件注册到线程池中
     *
     * @param support thread pool plugin manager delegate
     */
    @Override
    public void doRegister(ThreadPoolPluginSupport support) {

    }
}

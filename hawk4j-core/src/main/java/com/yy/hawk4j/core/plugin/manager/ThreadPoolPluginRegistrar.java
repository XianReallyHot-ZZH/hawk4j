package com.yy.hawk4j.core.plugin.manager;

import com.yy.hawk4j.core.plugin.ThreadPoolPlugin;

/**
 * Registrar of {@link ThreadPoolPlugin}.
 * <p>
 * 线程池插件注册器，负责将线程池插件注册到线程池中
 */
public interface ThreadPoolPluginRegistrar {

    /**
     * Get id
     *
     * @return id
     */
    default String getId() {
        return this.getClass().getSimpleName();
    }

    /**
     * Create and register plugin for the specified thread-pool instance.
     * 将内置的线程池插件注册到线程池中
     *
     * @param support thread pool plugin manager delegate
     */
    void doRegister(ThreadPoolPluginSupport support);

}

package com.yy.hawk4j.core.plugin.manager;

import com.yy.hawk4j.core.plugin.*;
import lombok.NonNull;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 赋予线程池插件管理体系能力，其实直接让ExtensibleThreadPoolExecutor继承ThreadPoolPluginManager也可以，效果是一样的，
 * 但是不够优雅，这里通过一层接口的继承，将有规律的、重复的接口实现代码作为接口的默认实现，定义在这里
 * Used to support the binding of {@link ThreadPoolPluginManager} and {@link ThreadPoolExecutor}.
 */
public interface ThreadPoolPluginSupport extends ThreadPoolPluginManager {

    /**
     * Get thread pool action aware registry.
     *
     * @return {@link ThreadPoolPluginManager}
     */
    @NonNull
    ThreadPoolPluginManager getThreadPoolPluginManager();

    /**
     * Get thread-pool id
     *
     * @return thread-pool id
     */
    String getThreadPoolId();

    /**
     * Get thread-pool executor.
     *
     * @return thread-pool executor
     */
    ThreadPoolExecutor getThreadPoolExecutor();

    // ======================== delegate methods 默认实现 ========================

    @Override
    default void clear() {
        getThreadPoolPluginManager().clear();
    }

    @Override
    default Collection<ThreadPoolPlugin> getAllPlugins() {
        return getThreadPoolPluginManager().getAllPlugins();
    }

    @Override
    default void register(ThreadPoolPlugin plugin) {
        getThreadPoolPluginManager().register(plugin);
    }

    @Override
    default boolean tryRegister(ThreadPoolPlugin plugin) {
        return getThreadPoolPluginManager().tryRegister(plugin);
    }

    @Override
    default boolean isRegistered(String pluginId) {
        return getThreadPoolPluginManager().isRegistered(pluginId);
    }

    @Override
    default void unregister(String pluginId) {
        getThreadPoolPluginManager().unregister(pluginId);
    }

    @Override
    default <A extends ThreadPoolPlugin> Optional<A> getPlugin(String pluginId) {
        return getThreadPoolPluginManager().getPlugin(pluginId);
    }

    @Override
    default Collection<RejectedAwarePlugin> getRejectedAwarePluginList() {
        return getThreadPoolPluginManager().getRejectedAwarePluginList();
    }

    @Override
    default Collection<TaskAwarePlugin> getTaskAwarePluginList() {
        return getThreadPoolPluginManager().getTaskAwarePluginList();
    }

    @Override
    default Collection<ExecuteAwarePlugin> getExecuteAwarePluginList() {
        return getThreadPoolPluginManager().getExecuteAwarePluginList();
    }

    @Override
    default Collection<ShutdownAwarePlugin> getShutdownAwarePluginList() {
        return getThreadPoolPluginManager().getShutdownAwarePluginList();
    }
}

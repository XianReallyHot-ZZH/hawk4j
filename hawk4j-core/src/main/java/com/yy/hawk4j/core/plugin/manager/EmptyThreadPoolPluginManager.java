package com.yy.hawk4j.core.plugin.manager;

import com.yy.hawk4j.core.plugin.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Empty thread pool plugin manager. 当默认实现使用
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EmptyThreadPoolPluginManager implements ThreadPoolPluginManager {

    /**
     * Default instance
     */
    public static final EmptyThreadPoolPluginManager INSTANCE = new EmptyThreadPoolPluginManager();

    /**
     * Clear all.
     */
    @Override
    public void clear() {

    }

    /**
     * Get all registered plugins.
     *
     * @return plugins
     */
    @Override
    public Collection<ThreadPoolPlugin> getAllPlugins() {
        return Collections.emptyList();
    }


    @Override
    public void register(ThreadPoolPlugin plugin) {

    }

    @Override
    public boolean tryRegister(ThreadPoolPlugin plugin) {
        return false;
    }

    @Override
    public boolean isRegistered(String pluginId) {
        return false;
    }

    @Override
    public void unregister(String pluginId) {

    }

    @Override
    public <A extends ThreadPoolPlugin> Optional<A> getPlugin(String pluginId) {
        return Optional.empty();
    }

    @Override
    public Collection<RejectedAwarePlugin> getRejectedAwarePluginList() {
        return Collections.emptyList();
    }

    @Override
    public Collection<TaskAwarePlugin> getTaskAwarePluginList() {
        return Collections.emptyList();
    }

    @Override
    public Collection<ExecuteAwarePlugin> getExecuteAwarePluginList() {
        return Collections.emptyList();
    }

    @Override
    public Collection<ShutdownAwarePlugin> getShutdownAwarePluginList() {
        return Collections.emptyList();
    }
}

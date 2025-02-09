package com.yy.hawk4j.core.plugin.manager;

import com.yy.hawk4j.core.plugin.*;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @框架核心点之一：插件体系设计之插件管理器设计
 * @线程池的插件管理器接口：负责对插件的管理，内部可以管理起来各种插件,任何插件的使用都需要先注册在这里
 *
 */
public interface ThreadPoolPluginManager {

    /**
     * Get an empty manager.
     *
     * @return {@link EmptyThreadPoolPluginManager}
     */
    static ThreadPoolPluginManager empty() {
        return EmptyThreadPoolPluginManager.INSTANCE;
    }


    /**
     * Clear all.
     */
    void clear();

    /**
     * Get all registered plugins.
     *
     * @return plugins
     */
    Collection<ThreadPoolPlugin> getAllPlugins();

    /**
     * Register a {@link ThreadPoolPlugin}.
     *
     * @param plugin {@link ThreadPoolPlugin}
     * @throws IllegalArgumentException thrown when a plugin with the same {@link ThreadPoolPlugin#getId()}
     *                                  already exists in the registry
     */
    void register(ThreadPoolPlugin plugin);

    /**
     * Register plugin if it's not registered.
     *
     * @param plugin plugin
     * @return return true if successful register new plugin, false otherwise
     */
    boolean tryRegister(ThreadPoolPlugin plugin);

    /**
     * Whether the {@link ThreadPoolPlugin} has been registered.
     *
     * @param pluginId plugin id
     * @return ture if target has been registered, false otherwise
     */
    boolean isRegistered(String pluginId);

    /**
     * Unregister {@link ThreadPoolPlugin}.
     *
     * @param pluginId plugin id
     */
    void unregister(String pluginId);

    /**
     * Get {@link ThreadPoolPlugin}.
     *
     * @param pluginId plugin id
     * @param <A>      target aware type
     * @return {@link ThreadPoolPlugin}
     * @throws ClassCastException thrown when the object obtained by name cannot be converted to target type
     */
    <A extends ThreadPoolPlugin> Optional<A> getPlugin(String pluginId);

    /**
     * Get rejected aware plugin list.
     *
     * @return {@link RejectedAwarePlugin}
     */
    Collection<RejectedAwarePlugin> getRejectedAwarePluginList();

    /**
     * Get task aware plugin list.
     *
     * @return {@link TaskAwarePlugin}
     */
    Collection<TaskAwarePlugin> getTaskAwarePluginList();

    /**
     * Get execute aware plugin list.
     *
     * @return {@link ExecuteAwarePlugin}
     */
    Collection<ExecuteAwarePlugin> getExecuteAwarePluginList();

    /**
     * Get shutdown aware plugin list.
     *
     * @return {@link ShutdownAwarePlugin}
     */
    Collection<ShutdownAwarePlugin> getShutdownAwarePluginList();

    // ======================================== default methods ========================================

    /**
     * Get plugin by plugin-id and type.
     *
     * @param pluginId   plugin id
     * @param pluginType plugin type
     * @return target plugin
     */
    default <A extends ThreadPoolPlugin> Optional<A> getPluginOfType(String pluginId, Class<A> pluginType) {
        return getPlugin(pluginId).filter(pluginType::isInstance).map(pluginType::cast);
    }

    /**
     * Get all plugins of type.
     *
     * @param pluginType plugin type
     * @return all plugins of type
     */
    default <A extends ThreadPoolPlugin> Collection<A> getAllPluginsOfType(Class<A> pluginType) {
        return getAllPlugins().stream().filter(pluginType::isInstance).map(pluginType::cast).collect(Collectors.toList());
    }

    /**
     * Get {@link PluginRuntime} of all registered plugins.
     *
     * @return {@link PluginRuntime} of all registered plugins
     */
    default Collection<PluginRuntime> getAllPluginRuntimes() {
        return getAllPlugins().stream()
                .map(ThreadPoolPlugin::getPluginRuntime)
                .collect(Collectors.toList());
    }

    /**
     * Get {@link PluginRuntime} of registered plugin.
     *
     * @return {@link PluginRuntime} of registered plugin
     */
    default Optional<PluginRuntime> getRuntime(String pluginId) {
        return getPlugin(pluginId)
                .map(ThreadPoolPlugin::getPluginRuntime);
    }

}

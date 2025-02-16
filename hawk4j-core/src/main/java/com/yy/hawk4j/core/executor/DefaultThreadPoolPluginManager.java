package com.yy.hawk4j.core.executor;

import com.yy.hawk4j.common.toolkit.Assert;
import com.yy.hawk4j.core.plugin.*;
import com.yy.hawk4j.core.plugin.manager.ThreadPoolPluginManager;
import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @方法描述：线程池的默认插件管理器，和线程池有关的所有插件都会注册到这个插件管理器中，每一个线程池都持有一个插件管理器对象
 */
public class DefaultThreadPoolPluginManager implements ThreadPoolPluginManager {

    /**
     * Lock of this instance
     * 控制整个类内数据的一致性，在涉及类内数据变更操作时，上锁，进行并发控制
     */
    private final ReadWriteLock instanceLock = new ReentrantReadWriteLock();

    /**
     * Registered {@link ThreadPoolPlugin}
     * key 为插件的id，value 为插件对象
     */
    private final Map<String, ThreadPoolPlugin> registeredPlugins = new ConcurrentHashMap<>(16);

    /**
     * Registered {@link TaskAwarePlugin}
     * 读多写少的场景
     */
    private final List<TaskAwarePlugin> taskAwarePluginList = new CopyOnWriteArrayList<>();

    /**
     * Registered {@link ExecuteAwarePlugin}
     */
    private final List<ExecuteAwarePlugin> executeAwarePluginList = new CopyOnWriteArrayList<>();

    /**
     * Registered {@link RejectedAwarePlugin}
     */
    private final List<RejectedAwarePlugin> rejectedAwarePluginList = new CopyOnWriteArrayList<>();

    /**
     * Registered {@link ShutdownAwarePlugin}
     */
    private final List<ShutdownAwarePlugin> shutdownAwarePluginList = new CopyOnWriteArrayList<>();


    /**
     * Clear all.
     */
    @Override
    public void clear() {
        // 控制整个类内数据一致性，进行并发控制
        Lock writeLock = instanceLock.writeLock();
        writeLock.lock();
        try {
            Collection<ThreadPoolPlugin> plugins = registeredPlugins.values();
            registeredPlugins.clear();
            taskAwarePluginList.clear();
            executeAwarePluginList.clear();
            rejectedAwarePluginList.clear();
            shutdownAwarePluginList.clear();
            plugins.forEach(ThreadPoolPlugin::stop);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Get all registered plugins.
     *
     * @return plugins
     * @apiNote Be sure to avoid directly modifying returned collection instances,
     * otherwise, unexpected results may be obtained through the manager
     */
    @Override
    public Collection<ThreadPoolPlugin> getAllPlugins() {
        Lock readLock = instanceLock.readLock();
        readLock.lock();
        try {
            return registeredPlugins.values();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Register a {@link ThreadPoolPlugin}
     *
     * @param plugin plugin
     * @throws IllegalArgumentException thrown when a plugin with the same {@link ThreadPoolPlugin#getId()} already exists in the registry
     * @see ThreadPoolPlugin#getId()
     */
    @Override
    public void register(@NonNull ThreadPoolPlugin plugin) {
        Lock writeLock = instanceLock.writeLock();
        writeLock.lock();
        try {
            String id = plugin.getId();
            Assert.isTrue(!isRegistered(id), "The plugin with id [" + id + "] has been registered");
            registeredPlugins.put(id, plugin);
            if (plugin instanceof TaskAwarePlugin) {
                taskAwarePluginList.add((TaskAwarePlugin) plugin);
            }
            if (plugin instanceof ExecuteAwarePlugin) {
                executeAwarePluginList.add((ExecuteAwarePlugin) plugin);
            }
            if (plugin instanceof RejectedAwarePlugin) {
                rejectedAwarePluginList.add((RejectedAwarePlugin) plugin);
            }
            if (plugin instanceof ShutdownAwarePlugin) {
                shutdownAwarePluginList.add((ShutdownAwarePlugin) plugin);
            }
            plugin.start();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Register plugin if it's not registered.
     *
     * @param plugin plugin
     * @return return true if successful register new plugin, false otherwise
     */
    @Override
    public boolean tryRegister(ThreadPoolPlugin plugin) {
        Lock writeLock = instanceLock.writeLock();
        writeLock.lock();
        try {
            if (registeredPlugins.containsKey(plugin.getId())) {
                return false;
            }
            register(plugin);
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Whether the {@link ThreadPoolPlugin} has been registered.
     *
     * @param pluginId plugin id
     * @return ture if target has been registered, false otherwise
     */
    @Override
    public boolean isRegistered(String pluginId) {
        Lock readLock = instanceLock.readLock();
        readLock.lock();
        try {
            return registeredPlugins.containsKey(pluginId);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Unregister {@link ThreadPoolPlugin}
     *
     * @param pluginId plugin id
     */
    @Override
    public void unregister(String pluginId) {
        Lock writeLock = instanceLock.writeLock();
        writeLock.lock();
        try {
            Optional.ofNullable(pluginId)
                    .map(registeredPlugins::remove)
                    .ifPresent(plugin -> {
                        if (plugin instanceof TaskAwarePlugin) {
                            taskAwarePluginList.remove(plugin);
                        }
                        if (plugin instanceof ExecuteAwarePlugin) {
                            executeAwarePluginList.remove(plugin);
                        }
                        if (plugin instanceof RejectedAwarePlugin) {
                            rejectedAwarePluginList.remove(plugin);
                        }
                        if (plugin instanceof ShutdownAwarePlugin) {
                            shutdownAwarePluginList.remove(plugin);
                        }
                        plugin.stop();
                    });
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Get {@link ThreadPoolPlugin}.
     *
     * @param pluginId plugin id
     * @param <A>      plugin type
     * @return {@link ThreadPoolPlugin}, null if unregister
     */
    @Override
    public <A extends ThreadPoolPlugin> Optional<A> getPlugin(String pluginId) {
        Lock readLock = instanceLock.readLock();
        readLock.lock();
        try {
            return (Optional<A>) Optional.ofNullable(registeredPlugins.get(pluginId));
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Get rejected plugin list.
     *
     * @return {@link RejectedAwarePlugin}
     * @apiNote Be sure to avoid directly modifying returned collection instances,
     * otherwise, unexpected results may be obtained through the manager
     */
    @Override
    public Collection<RejectedAwarePlugin> getRejectedAwarePluginList() {
        Lock readLock = instanceLock.readLock();
        readLock.lock();
        try {
            return rejectedAwarePluginList;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Get shutdown plugin list.
     *
     * @return {@link ShutdownAwarePlugin}
     * @apiNote Be sure to avoid directly modifying returned collection instances,
     * otherwise, unexpected results may be obtained through the manager
     */
    @Override
    public Collection<TaskAwarePlugin> getTaskAwarePluginList() {
        Lock readLock = instanceLock.readLock();
        readLock.lock();
        try {
            return taskAwarePluginList;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Get execute plugin list.
     *
     * @return {@link ExecuteAwarePlugin}
     */
    @Override
    public Collection<ExecuteAwarePlugin> getExecuteAwarePluginList() {
        Lock readLock = instanceLock.readLock();
        readLock.lock();
        try {
            return executeAwarePluginList;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Get shutdown plugin list.
     *
     * @return {@link ShutdownAwarePlugin}
     * @apiNote Be sure to avoid directly modifying returned collection instances,
     * otherwise, unexpected results may be obtained through the manager
     */
    @Override
    public Collection<ShutdownAwarePlugin> getShutdownAwarePluginList() {
        Lock readLock = instanceLock.readLock();
        readLock.lock();
        try {
            return shutdownAwarePluginList;
        } finally {
            readLock.unlock();
        }
    }
}

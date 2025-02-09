package com.yy.hawk4j.core.plugin;

import com.yy.hawk4j.core.plugin.manager.ThreadPoolPluginManager;

/**
 * @框架核心点之一：插件体系设计之插件本身
 * @插件基础接口
 */
public interface ThreadPoolPlugin {

    /**
     * 插件唯一标识,插件id
     *
     * @return id
     */
    String getId();

    /**
     * 插件start方法执行时机：当插件注册到插件管理器中时，完成对该插件的start方法回调
     *
     * @see ThreadPoolPluginManager#register
     */
    default void start() {}

    /**
     * 插件stop方法执行时机：当插件从插件管理器中时取消注册时，完成对该插件的stop方法回调
     *
     * @see ThreadPoolPluginManager#unregister
     * @see ThreadPoolPluginManager#clear
     */
    default void stop() {}

    /**
     * 获取到当前插件的运行时信息对象
     *
     * @return plugin runtime info {@link PluginRuntime}
     */
    default PluginRuntime getPluginRuntime() {
        return new PluginRuntime(getId());
    }

}

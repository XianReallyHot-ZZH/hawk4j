package com.yy.hawk4j.core.plugin.impl;

import com.yy.hawk4j.core.plugin.RejectedAwarePlugin;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @方法描述：拒绝任务的时候执行通知告警操作的插件
 */
public class TaskRejectNotifyAlarmPlugin implements RejectedAwarePlugin {

    public static final String PLUGIN_NAME = "task-reject-notify-alarm-plugin";

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
     * TODO:调用通知模块完成告警操作，等后续实现了通知相关能力后，这里再做实现
     *
     * @param runnable task
     * @param executor executor
     */
    @Override
    public void beforeRejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {

    }
}

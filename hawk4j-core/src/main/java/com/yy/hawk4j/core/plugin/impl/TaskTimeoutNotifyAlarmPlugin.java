package com.yy.hawk4j.core.plugin.impl;

import com.yy.hawk4j.core.plugin.PluginRuntime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @方法描述：这个插件就是用来判断线程中的任务执行是否超时了，如果超时了就调用processTaskTime方法执行告警操作
 */
@AllArgsConstructor
public class TaskTimeoutNotifyAlarmPlugin extends AbstractTaskTimerPlugin {

    public static final String PLUGIN_NAME = "task-timeout-notify-alarm-plugin";

    /**
     * Execute time-out
     */
    @Getter
    @Setter
    private Long executeTimeOut;

    /**
     * Thread-pool id
     */
    private final String threadPoolId;

    /**
     * Thread-pool executor
     */
    private final ThreadPoolExecutor threadPoolExecutor;

    @Override
    public String getId() {
        return PLUGIN_NAME;
    }

    /**
     * TODO:后续这里的具体实现为当执行超时了，就调用通知模块完成告警操作，等后续实现了通知相关能力后，这里再做实现
     * @param taskExecuteTime execute time of task
     */
    @Override
    protected void processTaskTime(Long taskExecuteTime) {
        if (taskExecuteTime > executeTimeOut) {
            System.out.println("发送超时告警通知...");
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
                .addInfo("executeTimeOut", this.executeTimeOut);
    }

}

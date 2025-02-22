package com.yy.hawk4j.core.plugin.impl;

import com.yy.hawk4j.core.plugin.PluginRuntime;
import com.yy.hawk4j.core.plugin.RejectedAwarePlugin;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @方法描述：决绝策略处理器的扩展插件
 */
public class TaskRejectCountRecordPlugin implements RejectedAwarePlugin {

    public static final String PLUGIN_NAME = "task-reject-count-record-plugin";

    /**
     * Rejection count
     */
    @Setter
    @Getter
    private AtomicLong rejectCount = new AtomicLong(0);

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
     * Record rejection count.
     *
     * @param runnable        task
     * @param executor executor
     */
    @Override
    public void beforeRejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
        rejectCount.incrementAndGet();
    }

    /**
     * Get plugin runtime info.
     *
     * @return plugin runtime info
     */
    @Override
    public PluginRuntime getPluginRuntime() {
        return new PluginRuntime(getId())
                .addInfo("rejectCount", getRejectCountNum());
    }

    /**
     * Get reject count num.
     *
     * @return reject count num
     */
    public Long getRejectCountNum() {
        return rejectCount.get();
    }

}

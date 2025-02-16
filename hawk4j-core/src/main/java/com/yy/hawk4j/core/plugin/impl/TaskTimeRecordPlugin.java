package com.yy.hawk4j.core.plugin.impl;

import com.yy.hawk4j.core.plugin.PluginRuntime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @方法描述：收集记录当前线程池执行任务的耗时指标（最大耗时、最小耗时、任务总耗时，任务总完成数等）
 */
@RequiredArgsConstructor
public class TaskTimeRecordPlugin extends AbstractTaskTimerPlugin {

    public static final String PLUGIN_NAME = "task-time-record-plugin";

    /**
     * Lock instance
     */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Total execution milli time of all tasks
     */
    private long totalTaskTimeMillis = 0L;

    /**
     * Maximum task milli execution time, default -1
     */
    private long maxTaskTimeMillis = -1L;

    /**
     * Minimal task milli execution time, default -1
     */
    private long minTaskTimeMillis = -1L;

    /**
     * Count of completed task
     */
    private long taskCount = 0L;

    @Override
    public String getId() {
        return PLUGIN_NAME;
    }

    @Override
    protected void processTaskTime(Long taskExecuteTime) {
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            if (taskCount == 0) {
                maxTaskTimeMillis = taskExecuteTime;
                minTaskTimeMillis = taskExecuteTime;
            } else {
                maxTaskTimeMillis = Math.max(taskExecuteTime, maxTaskTimeMillis);
                minTaskTimeMillis = Math.min(taskExecuteTime, minTaskTimeMillis);
            }
            taskCount = taskCount + 1;
            totalTaskTimeMillis += taskExecuteTime;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public PluginRuntime getPluginRuntime() {
        Summary summary = summarize();
        return new PluginRuntime(getId())
                .addInfo("taskCount", summary.getTaskCount())
                .addInfo("minTaskTime", summary.getMinTaskTimeMillis() + "ms")
                .addInfo("maxTaskTime", summary.getMaxTaskTimeMillis() + "ms")
                .addInfo("totalTaskTime", summary.getTotalTaskTimeMillis() + "ms")
                .addInfo("avgTaskTime", summary.getAvgTaskTimeMillis() + "ms");
    }


    /**
     * Get the summary statistics of the instance at the current time.
     * 在某一时刻进行指标统计
     *
     * @return data snapshot
     */
    public Summary summarize() {
        Lock readLock = lock.readLock();
        Summary statistics;
        readLock.lock();
        try {
            statistics = new Summary(
                    this.totalTaskTimeMillis,
                    this.maxTaskTimeMillis,
                    this.minTaskTimeMillis,
                    this.taskCount);
        } finally {
            readLock.unlock();
        }
        return statistics;
    }


    /**
     * Summary statistics of SyncTimeRecorder instance at a certain time.
     * 某一时刻的指标统计结果
     */
    @Getter
    @RequiredArgsConstructor
    public static class Summary {

        /**
         * Total execution nano time of all tasks
         */
        private final long totalTaskTimeMillis;

        /**
         * Maximum task nano execution time
         */
        private final long maxTaskTimeMillis;

        /**
         * Minimal task nano execution time
         */
        private final long minTaskTimeMillis;

        /**
         * Count of completed task
         */
        private final long taskCount;

        /**
         * Get the avg task time in milliseconds
         *
         * @return avg task time
         */
        public long getAvgTaskTimeMillis() {
            long totalTaskCount = getTaskCount();
            return totalTaskCount > 0L ? getTotalTaskTimeMillis() / totalTaskCount : -1;
        }
    }
}

package com.yy.hawk4j.core.toolkit;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 模拟一个系统时钟，可以获取到当前时间戳值
 * System clock.<br>
 * Refer to cn.hutool.core.date.SystemClock<br>
 */
public class SystemClock {

    /**
     * Period
     * 时钟刷新周期
     */
    private final int period;

    /**
     * Now
     * 当前时间戳值
     */
    private final AtomicLong now;

    /**
     * Thread name
     */
    private static final String THREAD_NAME = "system.clock";

    private SystemClock(int period) {
        this.period = period;
        this.now = new AtomicLong(System.currentTimeMillis());
        scheduleClockUpdating();
    }

    /**
     * Schedule clock updating.
     * 时钟更新跳动
     */
    private void scheduleClockUpdating() {
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1, runnable -> {
            Thread thread = new Thread(runnable, THREAD_NAME);
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(() -> now.set(System.currentTimeMillis()), period, period, TimeUnit.MILLISECONDS);
    }

    /**
     * Current time millis.
     *
     * @return current time millis
     */
    private long currentTimeMillis() {
        return now.get();
    }

    /**
     * Instance holder.
     */
    private static class InstanceHolder {

        /**
         * System clock instance
         */
        private static final SystemClock INSTANCE = new SystemClock(1);
    }

    /**
     * Instance.
     *
     * @return System clock instance
     */
    private static SystemClock instance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Now.
     *
     * @return current time millis
     */
    public static long now() {
        return instance().currentTimeMillis();
    }

}

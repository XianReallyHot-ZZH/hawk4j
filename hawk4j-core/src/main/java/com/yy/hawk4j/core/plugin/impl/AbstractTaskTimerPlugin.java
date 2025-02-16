package com.yy.hawk4j.core.plugin.impl;

import com.yy.hawk4j.core.plugin.ExecuteAwarePlugin;
import com.yy.hawk4j.core.toolkit.SystemClock;

import java.util.Optional;

/**
 * @方法描述：计算任务执行耗时的抽象插件，后续基于该插件可实现出针对耗时做出具体操作执行的插件
 */
public abstract class AbstractTaskTimerPlugin implements ExecuteAwarePlugin {

    /**
     * 记录每个线程执行的任务开始时间
     */
    private final ThreadLocal<Long> startTime = new ThreadLocal<>();

    //任务开始执行之前会执行这个方法，在这个方法中把任务开始执行时间放到线程本地map中
    @Override
    public void beforeExecute(Thread thread, Runnable runnable) {
        startTime.set(currentTime());
    }

    //该方法会在任务执行之后被调用
    @Override
    public void afterExecute(Runnable runnable, Throwable throwable) {
        try {
            Optional.ofNullable(startTime.get())
                    .map(startTime -> currentTime() - startTime)
                    .ifPresent(this::processTaskTime);
        } finally {
            // 清除线程变量，避免内存泄漏
            startTime.remove();
        }
    }

    /**
     * Get the current time.
     *
     * @return current time
     */
    protected long currentTime() {
        return SystemClock.now();
    }

    /**
     * Processing the execution time of the task.
     * 对任务耗时进行具体处理，交由具体的子类实现
     *
     * @param taskExecuteTime execute time of task
     */
    protected abstract void processTaskTime(Long taskExecuteTime);

}

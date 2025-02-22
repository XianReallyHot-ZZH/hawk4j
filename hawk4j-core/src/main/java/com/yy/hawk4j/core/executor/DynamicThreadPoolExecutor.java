package com.yy.hawk4j.core.executor;

import com.yy.hawk4j.common.toolkit.CollectionUtil;
import com.yy.hawk4j.core.plugin.impl.*;
import com.yy.hawk4j.core.plugin.manager.DefaultThreadPoolPluginManager;
import com.yy.hawk4j.core.plugin.manager.DefaultThreadPoolPluginRegistrar;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.task.TaskDecorator;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class DynamicThreadPoolExecutor extends ExtensibleThreadPoolExecutor implements DisposableBean {

    //关闭线程池时是否等待任务执行完毕
    @Getter
    @Setter
    public boolean waitForTasksToCompleteOnShutdown;

    public DynamicThreadPoolExecutor(
            int corePoolSize, int maximumPoolSize,
            long keepAliveTime, TimeUnit unit,
            long executeTimeOut, boolean waitForTasksToCompleteOnShutdown, long awaitTerminationMillis,
            @NonNull BlockingQueue<Runnable> blockingQueue,
            @NonNull String threadPoolId,
            @NonNull ThreadFactory threadFactory,
            @NonNull RejectedExecutionHandler rejectedExecutionHandler) {
        super(threadPoolId, new DefaultThreadPoolPluginManager(),
                corePoolSize, maximumPoolSize, keepAliveTime, unit,
                blockingQueue, threadFactory, rejectedExecutionHandler);
        log.info("Initializing ExecutorService {}", threadPoolId);
        this.waitForTasksToCompleteOnShutdown = waitForTasksToCompleteOnShutdown;
        //在这里，把所有内齿的插件都注册到当前动态线程池对象的插件管理器中了
        new DefaultThreadPoolPluginRegistrar(executeTimeOut, awaitTerminationMillis).doRegister(this);
    }

    @Override
    public void destroy() throws Exception {
        if (isWaitForTasksToCompleteOnShutdown()) {
            super.shutdown();
        } else {
            super.shutdownNow();
        }//在这里清空插件管理器中的插件
        getThreadPoolPluginManager().clear();
    }

    //得到ThreadPoolExecutorShutdownPlugin对象中awaitTerminationMillis的方法
    @Deprecated
    public long getAwaitTerminationMillis() {
        return getPluginOfType(ThreadPoolExecutorShutdownPlugin.PLUGIN_NAME, ThreadPoolExecutorShutdownPlugin.class)
                .map(ThreadPoolExecutorShutdownPlugin::getAwaitTerminationMillis)
                .orElse(-1L);
    }

    //设置ThreadPoolExecutorShutdownPlugin对象中awaitTerminationMillis的方法
    @Deprecated
    public void setSupportParam(long awaitTerminationMillis, boolean waitForTasksToCompleteOnShutdown) {
        setWaitForTasksToCompleteOnShutdown(waitForTasksToCompleteOnShutdown);
        getPluginOfType(ThreadPoolExecutorShutdownPlugin.PLUGIN_NAME, ThreadPoolExecutorShutdownPlugin.class)
                .ifPresent(processor -> processor.setAwaitTerminationMillis(awaitTerminationMillis));
    }


    //得到被拒绝的任务数量
    @Deprecated
    public Long getRejectCountNum() {
        return getPluginOfType(TaskRejectCountRecordPlugin.PLUGIN_NAME, TaskRejectCountRecordPlugin.class)
                .map(TaskRejectCountRecordPlugin::getRejectCountNum)
                .orElse(-1L);
    }


    //获得计算被拒绝的任务的计数器
    @Deprecated
    public AtomicLong getRejectCount() {
        return getPluginOfType(TaskRejectCountRecordPlugin.PLUGIN_NAME, TaskRejectCountRecordPlugin.class)
                .map(TaskRejectCountRecordPlugin::getRejectCount)
                .orElse(new AtomicLong(0));
    }


    //得到任务执行超时时间
    @Deprecated
    public Long getExecuteTimeOut() {
        return getPluginOfType(TaskTimeoutNotifyAlarmPlugin.PLUGIN_NAME, TaskTimeoutNotifyAlarmPlugin.class)
                .map(TaskTimeoutNotifyAlarmPlugin::getExecuteTimeOut)
                .orElse(-1L);
    }


    //设置超时时间
    @Deprecated
    public void setExecuteTimeOut(Long executeTimeOut) {
        getPluginOfType(TaskTimeoutNotifyAlarmPlugin.PLUGIN_NAME, TaskTimeoutNotifyAlarmPlugin.class)
                .ifPresent(processor -> processor.setExecuteTimeOut(executeTimeOut));
    }

    //得到任务执行总时间
    @Deprecated
    public long getTotalTaskTimeMillis() {
        return getPluginOfType(TaskTimeRecordPlugin.PLUGIN_NAME, TaskTimeRecordPlugin.class)
                .map(TaskTimeRecordPlugin::getTotalTaskTimeMillis)
                .orElse(-1L);
    }

    //得到任务执行最大耗时
    @Deprecated
    public long getMaxTaskTimeMillis() {
        return getPluginOfType(TaskTimeRecordPlugin.PLUGIN_NAME, TaskTimeRecordPlugin.class)
                .map(TaskTimeRecordPlugin::getMaxTaskTimeMillis)
                .orElse(-1L);
    }

    //得到任务执行最小耗时
    @Deprecated
    public long getMinTaskTimeMillis() {
        return getPluginOfType(TaskTimeRecordPlugin.PLUGIN_NAME, TaskTimeRecordPlugin.class)
                .map(TaskTimeRecordPlugin::getMinTaskTimeMillis)
                .orElse(-1L);
    }

    // 得到任务执行数
    @Deprecated
    public long getCompletedTaskCount() {
        return getPluginOfType(TaskTimeRecordPlugin.PLUGIN_NAME, TaskTimeRecordPlugin.class)
                .map(TaskTimeRecordPlugin::getTaskCount)
                .orElse(-1L);
    }

    //得到任务装饰器对象
    @Deprecated
    public TaskDecorator getTaskDecorator() {
        return getPluginOfType(TaskDecoratorPlugin.PLUGIN_NAME, TaskDecoratorPlugin.class)
                .map(processor -> CollectionUtil.getFirst(processor.getDecorators()))
                .orElse(null);
    }


    //设置任务装饰器对象
    @Deprecated
    public void setTaskDecorator(TaskDecorator taskDecorator) {
        getPluginOfType(TaskDecoratorPlugin.PLUGIN_NAME, TaskDecoratorPlugin.class)
                .ifPresent(processor -> {
                    if (Objects.nonNull(taskDecorator)) {
                        processor.clearDecorators();
                        processor.addDecorator(taskDecorator);
                    }
                });
    }


    //得到拒绝策略处理器
    @Deprecated
    public RejectedExecutionHandler getRedundancyHandler() {
        return getRejectedExecutionHandler();
    }


    //设置拒绝策略处理器
    @Deprecated
    public void setRedundancyHandler(RejectedExecutionHandler handler) {
        setRejectedExecutionHandler(handler);
    }
}

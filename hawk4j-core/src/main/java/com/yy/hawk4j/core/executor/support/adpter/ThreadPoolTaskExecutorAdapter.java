package com.yy.hawk4j.core.executor.support.adpter;

import com.yy.hawk4j.common.toolkit.ReflectUtil;
import com.yy.hawk4j.core.executor.DynamicThreadPoolExecutor;
import com.yy.hawk4j.core.executor.support.ThreadPoolBuilder;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @类描述：（内置）ThreadPoolTaskExecutor第三方线程池的适配器
 */
public class ThreadPoolTaskExecutorAdapter implements DynamicThreadPoolAdapter {

    private static final String EXECUTOR_FIELD_NAME = "threadPoolExecutor";


    private static final String WAIT_FOR_TASKS_TO_COMPLETE_ON_SHUTDOWN = "waitForTasksToCompleteOnShutdown";


    private static final String AWAIT_TERMINATION_MILLIS = "awaitTerminationMillis";


    private static final String TASK_DECORATOR = "taskDecorator";


    private static final String BEAN_NAME = "beanName";


    private static final String QUEUE_CAPACITY = "queueCapacity";


    private static String MATCH_CLASS_NAME = "ThreadPoolTaskExecutor";

    @Override
    public boolean match(Object executor) {
        return Objects.equals(MATCH_CLASS_NAME, executor.getClass().getSimpleName());
    }

    @Override
    public DynamicThreadPoolExecutor unwrap(Object executor) {
        Object unwrap = ReflectUtil.getFieldValue(executor, EXECUTOR_FIELD_NAME);
        if (unwrap == null) {
            return null;
        }
        if (!(unwrap instanceof ThreadPoolExecutor)) {
            return null;
        }
        if (unwrap instanceof DynamicThreadPoolExecutor) {
            return (DynamicThreadPoolExecutor) unwrap;
        }
        // 如果内部是一个普通的ThreadPoolExecutor，那么就创建一个动态线程池，用于后续替换掉本来那个普通的
        boolean waitForTasksToCompleteOnShutdown = (boolean) ReflectUtil.getFieldValue(executor, WAIT_FOR_TASKS_TO_COMPLETE_ON_SHUTDOWN);
        long awaitTerminationMillis = (long) ReflectUtil.getFieldValue(executor, AWAIT_TERMINATION_MILLIS);
        String beanName = (String) ReflectUtil.getFieldValue(executor, BEAN_NAME);
        int queueCapacity = (int) ReflectUtil.getFieldValue(executor, QUEUE_CAPACITY);
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) unwrap;
        ThreadPoolTaskExecutor threadPoolTaskExecutor = (ThreadPoolTaskExecutor) executor;
        ThreadPoolBuilder threadPoolBuilder = ThreadPoolBuilder.builder()
                .dynamicPool()
                .corePoolSize(threadPoolTaskExecutor.getCorePoolSize())
                .maxPoolNum(threadPoolTaskExecutor.getMaxPoolSize())
                .keepAliveTime(threadPoolTaskExecutor.getKeepAliveSeconds())
                .timeUnit(TimeUnit.SECONDS)
                .allowCoreThreadTimeOut(threadPoolExecutor.allowsCoreThreadTimeOut())
                .waitForTasksToCompleteOnShutdown(waitForTasksToCompleteOnShutdown)
                .awaitTerminationMillis(awaitTerminationMillis)
                .threadFactory(threadPoolExecutor.getThreadFactory())
                .threadPoolId(beanName)
                .rejected(threadPoolExecutor.getRejectedExecutionHandler());
        threadPoolBuilder.capacity(queueCapacity);
        Optional.ofNullable(ReflectUtil.getFieldValue(executor, TASK_DECORATOR))
                .ifPresent((taskDecorator) -> threadPoolBuilder.taskDecorator((TaskDecorator) taskDecorator));
        return (DynamicThreadPoolExecutor) threadPoolBuilder.build();
    }

    @Override
    public void replace(Object executor, Executor dynamicThreadPoolExecutor) {
        ReflectUtil.setFieldValue(executor, EXECUTOR_FIELD_NAME, dynamicThreadPoolExecutor);
    }
}

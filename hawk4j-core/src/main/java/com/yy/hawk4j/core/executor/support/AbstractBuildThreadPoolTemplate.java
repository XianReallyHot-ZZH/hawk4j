package com.yy.hawk4j.core.executor.support;

import com.yy.hawk4j.common.design.builder.ThreadFactoryBuilder;
import com.yy.hawk4j.core.executor.DynamicThreadPoolExecutor;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskDecorator;
import org.springframework.util.Assert;

import java.util.concurrent.*;

/**
 * @方法描述：线程池模板类，框架中的线程池就是由这类创建的，不管是动态线程池还是快速线程池还是普通线程池，都是由这个类创建的,创建的具体实现过程都在这个类里面
 */
@Slf4j
public class AbstractBuildThreadPoolTemplate {

    /**
     * 用于初始化一个线程池的参数合集对象
     * @return
     */
    protected static ThreadPoolInitParam initParam() {
        throw new UnsupportedOperationException();
    }

    /**
     * 创建一个动态线程池
     *
     * @param initParam
     * @return
     */
    public static DynamicThreadPoolExecutor buildDynamicPool(ThreadPoolInitParam initParam) {
        Assert.notNull(initParam);
        DynamicThreadPoolExecutor dynamicThreadPoolExecutor;
        try {
            dynamicThreadPoolExecutor = new DynamicThreadPoolExecutor(
                    initParam.getCorePoolNum(),
                    initParam.getMaxPoolNum(),
                    initParam.getKeepAliveTime(),
                    initParam.getTimeUnit(),
                    initParam.getExecuteTimeOut(),
                    initParam.getWaitForTasksToCompleteOnShutdown(),
                    initParam.getAwaitTerminationMillis(),
                    initParam.getWorkQueue(),
                    initParam.getThreadPoolId(),
                    initParam.getThreadFactory(),
                    initParam.getRejectedExecutionHandler());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(String.format("Error creating thread pool parameter. threadPool id: %s", initParam.getThreadPoolId()), ex);
        }
        dynamicThreadPoolExecutor.setTaskDecorator(initParam.getTaskDecorator());
        dynamicThreadPoolExecutor.allowCoreThreadTimeOut(initParam.allowCoreThreadTimeOut);
        return dynamicThreadPoolExecutor;
    }

    /**
     * 创建一个快速线程池
     *
     * @param initParam
     * @return
     */
    public static ThreadPoolExecutor buildFastPool(ThreadPoolInitParam initParam) {
        return null;
    }

    public static ThreadPoolExecutor buildFastPool() {
        ThreadPoolInitParam initParam = initParam();
        return buildFastPool(initParam);
    }

    /**
     * 创建一个普通线程池
     *
     * @param initParam
     * @return
     */
    public static ThreadPoolExecutor buildPool(ThreadPoolInitParam initParam) {
        return null;
    }

    public static ThreadPoolExecutor buildPool() {
        ThreadPoolInitParam initParam = initParam();
        return buildPool(initParam);
    }

    /**
     * 用于初始化一个线程池的参数合集对象
     * <p>
     * @方法描述：这个内部类的对象封装了线程池的核心参数，最后就是用它来创建线程池的
     */
    @Data
    @Accessors(chain = true)
    public static class ThreadPoolInitParam {
        private Integer corePoolNum;

        private Integer maxPoolNum;

        private Long keepAliveTime;

        private TimeUnit timeUnit;

        private Long executeTimeOut;

        private Integer capacity;

        private BlockingQueue<Runnable> workQueue;

        private RejectedExecutionHandler rejectedExecutionHandler;

        private ThreadFactory threadFactory;

        private String threadPoolId;

        private TaskDecorator taskDecorator;

        private Long awaitTerminationMillis;

        private Boolean waitForTasksToCompleteOnShutdown;

        private Boolean allowCoreThreadTimeOut = false;

        public ThreadPoolInitParam(String threadNamePrefix, boolean isDaemon) {
            this.threadFactory = ThreadFactoryBuilder.builder()
                    .prefix(threadNamePrefix)
                    .daemon(isDaemon)
                    .build();
        }

        public ThreadPoolInitParam(ThreadFactory threadFactory) {
            this.threadFactory = threadFactory;
        }
    }

}

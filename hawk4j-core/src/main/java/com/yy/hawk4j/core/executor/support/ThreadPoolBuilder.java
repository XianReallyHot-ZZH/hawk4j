package com.yy.hawk4j.core.executor.support;

import com.yy.hawk4j.common.design.builder.Builder;
import com.yy.hawk4j.common.executor.support.BlockingQueueTypeEnum;
import com.yy.hawk4j.common.toolkit.Assert;
import org.springframework.core.task.TaskDecorator;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * @方法描述：线程池构建器（覆盖对动态线程池、快速线程池、普通线程池的构建能力）
 */
public class ThreadPoolBuilder implements Builder<ThreadPoolExecutor> {

    //这个表示线程池构建器是否要创建快速线程池，快速线程池就是执行任务比较快一点的线程池
    private boolean isFastPool;

    //表示这个线程池构建器是否要创建动态线程池
    private boolean isDynamicPool;

    //得到线程池的核心线程数量，这个数量是根据CPU核心数量计算出来的
    private int corePoolSize = calculateCoreNum();

    //线程池最大线程数量
    private int maxPoolSize = corePoolSize + (corePoolSize >> 1);

    //存活时间
    private long keepAliveTime = 30000L;

    //时间单位
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    //执行任务超时时间
    private long executeTimeOut = 10000L;

    //队列容量
    private int capacity = 512;

    //队列类型，默认使用LinkedBlockingQueue队列
    private BlockingQueueTypeEnum blockingQueueType = BlockingQueueTypeEnum.LINKED_BLOCKING_QUEUE;

    //线程池队列
    private BlockingQueue<Runnable> workQueue;

    //拒绝策略
    private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();

    //是否为守护线程
    private boolean isDaemon = false;

    //线程池现成的前缀名称
    private String threadNamePrefix;

    //创建线程的线程工厂
    private ThreadFactory threadFactory;

    //线程池Id
    private String threadPoolId;

    //线程池要执行的任务的装饰器，这个装饰器很有用，是一个挺好的扩展点
    //在这个装饰器的包装下，用户可以在线程池要执行的真正任务之前做一些额外的操作
    //在这个动态线程池框架中， 有一个TaskTraceBuilderHandler对象，这就是一个装饰器对象
    //在它的包装下，在每一个任务执行之前，都会把任务超时标志放到执行人物的线程中
    //如果线程执行的某个任务超时了，就可以使用这个标志告警通知了
    private TaskDecorator taskDecorator;

    //线程池关闭时，等待剩余任务执行的最大时间
    private Long awaitTerminationMillis = 5000L;

    //线程池关闭时是否等待正在执行的任务执行完毕
    private Boolean waitForTasksToCompleteOnShutdown = true;

    //是否允许超过存活时间的核心线程终止工作
    private Boolean allowCoreThreadTimeOut = false;

    //得到线程池核心线程数量的方法
    private Integer calculateCoreNum() {
        int cpuCoreNum = Runtime.getRuntime().availableProcessors();
        return new BigDecimal(cpuCoreNum).divide(new BigDecimal("0.2")).intValue();
    }

    public ThreadPoolBuilder isFastPool(Boolean isFastPool) {
        this.isFastPool = isFastPool;
        return this;
    }

    public ThreadPoolBuilder dynamicPool() {
        this.isDynamicPool = true;
        return this;
    }

    public ThreadPoolBuilder threadFactory(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
        return this;
    }

    public ThreadPoolBuilder threadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    public ThreadPoolBuilder threadFactory(String threadNamePrefix, Boolean isDaemon) {
        this.threadNamePrefix = threadNamePrefix;
        this.isDaemon = isDaemon;
        return this;
    }

    public ThreadPoolBuilder corePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
        return this;
    }

    public ThreadPoolBuilder maxPoolNum(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        if (maxPoolSize < this.corePoolSize) {
            this.corePoolSize = maxPoolSize;
        }
        return this;
    }

    public ThreadPoolBuilder singlePool() {
        int singleNum = 1;
        this.corePoolSize = singleNum;
        this.maxPoolSize = singleNum;
        return this;
    }

    public ThreadPoolBuilder singlePool(String threadNamePrefix) {
        int singleNum = 1;
        this.corePoolSize = singleNum;
        this.maxPoolSize = singleNum;
        this.threadNamePrefix = threadNamePrefix;
        return this;
    }

    public ThreadPoolBuilder poolThreadSize(int corePoolSize, int maxPoolSize) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        return this;
    }

    public ThreadPoolBuilder keepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
        return this;
    }

    public ThreadPoolBuilder timeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        return this;
    }

    public ThreadPoolBuilder executeTimeOut(long executeTimeOut) {
        this.executeTimeOut = executeTimeOut;
        return this;
    }

    public ThreadPoolBuilder keepAliveTime(long keepAliveTime, TimeUnit timeUnit) {
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        return this;
    }

    public ThreadPoolBuilder capacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    public ThreadPoolBuilder workQueue(BlockingQueueTypeEnum queueType, int capacity) {
        this.blockingQueueType = queueType;
        this.capacity = capacity;
        return this;
    }

    public ThreadPoolBuilder rejected(RejectedExecutionHandler rejectedExecutionHandler) {
        this.rejectedExecutionHandler = rejectedExecutionHandler;
        return this;
    }

    public ThreadPoolBuilder workQueue(BlockingQueueTypeEnum blockingQueueType) {
        this.blockingQueueType = blockingQueueType;
        return this;
    }

    public ThreadPoolBuilder workQueue(BlockingQueue workQueue) {
        this.workQueue = workQueue;
        return this;
    }

    public ThreadPoolBuilder threadPoolId(String threadPoolId) {
        this.threadPoolId = threadPoolId;
        return this;
    }

    public ThreadPoolBuilder taskDecorator(TaskDecorator taskDecorator) {
        this.taskDecorator = taskDecorator;
        return this;
    }

    public ThreadPoolBuilder awaitTerminationMillis(long awaitTerminationMillis) {
        this.awaitTerminationMillis = awaitTerminationMillis;
        return this;
    }

    public ThreadPoolBuilder waitForTasksToCompleteOnShutdown(boolean waitForTasksToCompleteOnShutdown) {
        this.waitForTasksToCompleteOnShutdown = waitForTasksToCompleteOnShutdown;
        return this;
    }

    public ThreadPoolBuilder dynamicSupport(boolean waitForTasksToCompleteOnShutdown, long awaitTerminationMillis) {
        this.awaitTerminationMillis = awaitTerminationMillis;
        this.waitForTasksToCompleteOnShutdown = waitForTasksToCompleteOnShutdown;
        return this;
    }

    public ThreadPoolBuilder allowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
        return this;
    }

    //得到线程池构建器的方法
    public static ThreadPoolBuilder builder() {
        return new ThreadPoolBuilder();
    }

    //通过线程池Id创建动态线程池的方法（除了线程id参数外，其他参数都默认）
    public static ThreadPoolExecutor buildDynamicPoolById(String threadPoolId) {
        return ThreadPoolBuilder.builder().threadFactory(threadPoolId).threadPoolId(threadPoolId).dynamicPool().build();
    }

    /**
     * 触发构建线程池(根据build参数构建线程池对象)
     *
     * @return
     */
    @Override
    public ThreadPoolExecutor build() {
        if (isDynamicPool) {
            return buildDynamicPool(this);
        }
        return isFastPool ? buildFastPool(this) : buildPool(this);
    }

    //创建普通线程池的方法
    private static ThreadPoolExecutor buildPool(ThreadPoolBuilder builder) {
        return AbstractBuildThreadPoolTemplate.buildPool(buildInitParam(builder));
    }


    //创建快速线程池的方法
    private static ThreadPoolExecutor buildFastPool(ThreadPoolBuilder builder) {
        return AbstractBuildThreadPoolTemplate.buildFastPool(buildInitParam(builder));
    }

    //创建动态线程池的方法
    private static ThreadPoolExecutor buildDynamicPool(ThreadPoolBuilder builder) {
        return AbstractBuildThreadPoolTemplate.buildDynamicPool(buildInitParam(builder));
    }

    /**
     * @方法描述：创建ThreadPoolInitParam对象的方法，这个对象中封装了线程池的核心参数信息，然后会使用这个ThreadPoolInitParam对象去创建真正的线程池
     *
     * @param builder
     * @return
     */
    private static AbstractBuildThreadPoolTemplate.ThreadPoolInitParam buildInitParam(ThreadPoolBuilder builder) {
        //定义一个ThreadPoolInitParam对象
        AbstractBuildThreadPoolTemplate.ThreadPoolInitParam initParam;
        //判断线程工厂是否为空，如果线程工厂为空，就判断线程池中线程前缀是否为空
        if (builder.threadFactory == null) {
            Assert.notEmpty(builder.threadNamePrefix, "The thread name prefix cannot be empty or an empty string.");
            //如果线程前缀不为空，则直接创建ThreadPoolInitParam对象即可，这里创建ThreadPoolInitParam对象的过程中，其实就把ThreadPoolInitParam
            //对象内部的ThreadFactory成员变量创建成功了
            initParam = new AbstractBuildThreadPoolTemplate.ThreadPoolInitParam(builder.threadNamePrefix, builder.isDaemon);
        } else {
            //如果线程工厂不为空，直接创建ThreadPoolInitParam对象即可
            initParam = new AbstractBuildThreadPoolTemplate.ThreadPoolInitParam(builder.threadFactory);
        }

        //接下来就要使用刚才得到的构造器对象给initParam中的其他成员变量赋值即可
        initParam.setCorePoolNum(builder.corePoolSize)
                //设置最大线程数
                .setMaxPoolNum(builder.maxPoolSize)
                //设置线程存活时间
                .setKeepAliveTime(builder.keepAliveTime)
                //设置队列容量
                .setCapacity(builder.capacity)
                //设置任务超时时间
                .setExecuteTimeOut(builder.executeTimeOut)
                //设置拒绝策略
                .setRejectedExecutionHandler(builder.rejectedExecutionHandler)
                //设置时间单位
                .setTimeUnit(builder.timeUnit)
                //设置核心线程超过存活时间是否存活
                .setAllowCoreThreadTimeOut(builder.allowCoreThreadTimeOut)
                //设置任务装饰器
                .setTaskDecorator(builder.taskDecorator);

        //判断用户要创建的是什么线程池
        if (builder.isDynamicPool) {
            //这里创建的就是动态线程池得到线程池Id
            String threadPoolId = Optional.ofNullable(builder.threadPoolId).orElse(builder.threadNamePrefix);
            //设置线程池Id
            initParam.setThreadPoolId(threadPoolId);
            //设置线程池关闭时是否等待正在执行的任务执行完毕
            initParam.setWaitForTasksToCompleteOnShutdown(builder.waitForTasksToCompleteOnShutdown);
            //设置线程池关闭时，等待剩余任务执行的最大时间
            initParam.setAwaitTerminationMillis(builder.awaitTerminationMillis);
        }

        //如果创建的不是快速线程池，那就意味着是普通线程池或者动态线程池
        if (!builder.isFastPool) {
            //判断任务队列是否为空
            if (builder.workQueue == null) {
                //设置任务队列，使用的是LinkedBlockingQueue
                if (builder.blockingQueueType == null) {
                    builder.blockingQueueType = BlockingQueueTypeEnum.LINKED_BLOCKING_QUEUE;
                }
                //创建LinkedBlockingQueue队列
                builder.workQueue = BlockingQueueTypeEnum.createBlockingQueue(builder.blockingQueueType.getType(), builder.capacity);
            }
            //设置任务队列
            initParam.setWorkQueue(builder.workQueue);
        }

        return initParam;
    }
}

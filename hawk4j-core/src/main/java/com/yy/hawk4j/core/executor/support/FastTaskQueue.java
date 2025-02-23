package com.yy.hawk4j.core.executor.support;

import lombok.Setter;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @类描述：这个是快速线程池专属的任务队列（快速线程池的实现需要重写阻塞队列的某些行为，所以这里需要写一个专属的任务队列）
 * @param <R>
 */
public class FastTaskQueue<R extends Runnable> extends LinkedBlockingQueue<Runnable> {

    private static final long serialVersionUID = 1L;

    @Setter
    private FastThreadPoolExecutor executor;

    public FastTaskQueue(int capacity) {
        super(capacity);
    }

    /**
     * 向任务队列中添加任务的方法，这个方法的逻辑要结合jdk的 {@link ThreadPoolExecutor#execute}方法来理解
     * <p>
     * 原理：通过execute向线程池提交任务的时候，会先调用队列的offer方法进行任务入队，入队失败，进一步触发线程池中非核心线程的创建并执行任务（如果线程还没到最大线程数）
     * 那么这里改造的逻辑就是当队列中等待的任务数大于核心线程数时但是线程池的线程数还没到最大线程池数，那么offer方法直接返回false，提前触发线程池中非核心线程的创建并执行任务
     *
     * @param runnable the element to add
     * @return
     */
    @Override
    public boolean offer(Runnable runnable) {
//首先获取线程池中当前现成的数量
        int currentPoolThreadSize = executor.getPoolSize();
        //如果已经提交但未完成的任务数量小于当前线程数量，那就直接把任务添加到队列中，让线程执行即可
        if (executor.getSubmittedTaskCount() < currentPoolThreadSize) {
            return super.offer(runnable);
        }
        //走到这里意味着提交的任务数量大于当前线程数量了，但是又判断了一下当前线程数量是否小于线程池的最大线程数量
        //如果小于就意味着线程池还可以继续创建线程，那就返回false，让线程池创建线程，再执行任务，这也就是快速线程池快速的原因
        if (currentPoolThreadSize < executor.getMaximumPoolSize()) {
            return false;
        }
        //走到这里意味着线程创建到最大了，就直接往队列中添加即可
        return super.offer(runnable);
    }

    //重新把任务放到队列的方法
    public boolean retryOffer(Runnable runnable, long timeout, TimeUnit unit) throws InterruptedException {
        if (executor.isShutdown()) {
            throw new RejectedExecutionException("Actuator closed!");
        }
        return super.offer(runnable, timeout, unit);
    }


}

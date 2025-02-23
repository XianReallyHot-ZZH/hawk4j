package com.yy.hawk4j.core.executor.support;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 快速线程池
 * <p>
 * 普通线程池：当核心线程池满后，将任务放置进队列中，当队列满后，会创建非核心线程池，当非核心线程池也满后，就会执行拒绝策略
 * <p>
 * 快速线程池：当核心线程池满后，会立刻创建非核心线程池，当非核心线程池也满后，将任务放置进队列中，当队列满后，执行拒绝策略
 */
@Slf4j
public class FastThreadPoolExecutor extends GeneralThreadPoolExecutor {

    //这个成员变量就是用来记录当前执行器中提交的任务数量（其实更确切地说应该是提交后还没执行完的任务）
    private final AtomicInteger submittedTaskCount = new AtomicInteger(0);

    //获得像执行器提交后未完成的任务数量
    public int getSubmittedTaskCount() {
        return submittedTaskCount.get();
    }

    public FastThreadPoolExecutor(int corePoolSize,
                                  int maximumPoolSize,
                                  long keepAliveTime,
                                  TimeUnit unit,
                                  FastTaskQueue<Runnable> workQueue,
                                  ThreadFactory threadFactory,
                                  RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    //这是线程池本身的一个扩展方法，该方法会在线程池执行完任务后执行
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        //因为已经执行完一个任务了，所以把提交任务数量减一
        submittedTaskCount.decrementAndGet();
    }

    //执行任务的方法
    @Override
    public void execute(Runnable command) {
        //提交任务的时候，首先让任务计数自增一
        submittedTaskCount.incrementAndGet();
        try {//调用父类方法执行任务，注意，这里调用父类方法之后，逻辑就会来到FastTaskQueue类中了
            //这是ThreadPoolExecutor线程池本身的逻辑，会先把任务交给任务队列，会调用起队列的offer方法
            super.execute(command);
        } catch (RejectedExecutionException rx) {
            //添加任务失败，触发了拒绝策略后
            final FastTaskQueue queue = (FastTaskQueue) super.getQueue();
            try {
                //重新尝试添加到队列中，这里为什么要重新尝试入队呢？？？有点想不通！！！
                if (!queue.retryOffer(command, 0, TimeUnit.MILLISECONDS)) {
                    //添加失败则不能执行该任务，提交任务计数减一
                    submittedTaskCount.decrementAndGet();
                    throw new RejectedExecutionException("The blocking queue capacity is full.", rx);
                }
            } catch (InterruptedException x) {
                submittedTaskCount.decrementAndGet();
                throw new RejectedExecutionException(x);
            }
        } catch (Exception t) {
            submittedTaskCount.decrementAndGet();
            throw t;
        }
    }
}

package com.yy.hawk4j.core.executor.support;

import com.yy.hawk4j.common.toolkit.ArrayUtil;

import java.util.concurrent.*;

/**
 * 类描述：普通线程池类，快速线程池会继承了这个普通线程池类，这个普通线程池类只是做了一层方便后续快速线程池继承的功能封装
 * <p>
 * （具体封装的内容就是在任务提交的时候对任务进行了一层当任务异常的时候，在异常堆栈里增加任务提交源头的信息,不然任务异常只能获取到当时线程的异常堆栈）
 */
public class GeneralThreadPoolExecutor extends ThreadPoolExecutor {
    public GeneralThreadPoolExecutor(int corePoolSize,
                                     int maximumPoolSize,
                                     long keepAliveTime,
                                     TimeUnit unit,
                                     BlockingQueue<Runnable> workQueue,
                                     ThreadFactory threadFactory,
                                     RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    public void execute(final Runnable command) {
        //在执行之前调用了clientTrace()方法，获得了一个异常对象
        //然后调用了wrap方法
        super.execute(wrap(command, clientTrace()));
    }

    @Override
    public Future<?> submit(final Runnable task) {
        return super.submit(wrap(task, clientTrace()));
    }

    @Override
    public <T> Future<T> submit(final Callable<T> task) {
        return super.submit(wrap(task, clientTrace()));
    }


    //获得异常对象的方法
    private Exception clientTrace() {
        return new Exception("Tread task root stack trace.");
    }

    //包装任务的方法，在这个方法中传进了一个异常对象，这个异常对象的作用就是在任务执行出现异常时，快速定位任务提交的源头(clientStack就是任务提交时候的源头信息)
    private Runnable wrap(final Runnable task, final Exception clientStack) {
        return () -> {
            try {
                task.run();
            } catch (Exception e) {
                e.setStackTrace(ArrayUtil.addAll(clientStack.getStackTrace(), e.getStackTrace()));
                throw e;
            }
        };
    }


    private <T> Callable<T> wrap(final Callable<T> task, final Exception clientStack) {
        return () -> {
            try {
                return task.call();
            } catch (Exception e) {
                e.setStackTrace(ArrayUtil.addAll(clientStack.getStackTrace(), e.getStackTrace()));
                throw e;
            }
        };
    }
}

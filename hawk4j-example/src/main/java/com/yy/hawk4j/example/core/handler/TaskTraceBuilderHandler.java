package com.yy.hawk4j.example.core.handler;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

/**
 * TODO:任务适配器，这个适配器的作用是就在线程池中的线程执行每一个任务之前，把任务超时的标志放到当前线程的本地变量中，这是用MDC实现的
 */
public final class TaskTraceBuilderHandler implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
//        //从MDC中取出存放在外层线程的任务超时标志，注意，当这个装饰器对象中的decorate方法执行的时候，还没有把任务提交给线程池
//        //所以现在执行这个方法的是把任务提交给线程池的线程，也就是我这里说的这个外层线程，如果我们把任务超时标志设置到外层线程中
//        //就可以在把任务提交给线程池之前，把任务超时标志从外层线程拿出来，然后设置到执行任务的线程池的线程中
//        String executeTimeoutTrace = MDC.get(EXECUTE_TIMEOUT_TRACE);
//
//        //创建任务，这个任务对线程池要执行的任务做了层包装，之后线程池就要执行这个taskRun任务了
//        Runnable taskRun = () -> {
//
//            //判断executeTimeoutTrace是否不为空
//            if (StringUtil.isNotBlank(executeTimeoutTrace)) {
//                //如果不为空，就把它设置到真正的线程池的线程中
//                ExecutorTraceContextUtil.putTimeoutTrace(executeTimeoutTrace);
//            }//设置完毕后执行用户定义的任务
//            runnable.run();
//            //这里本来该清理标志对吧？但是没有清理，这是为什么？整个狂家长只有当任务真的超时后，会在发送报警请求时把这个标志从MDC中清楚了
//            //其他时候都没有清除，这是为什么？
//        };
//        return taskRun;


        return runnable;
    }
}

package com.yy.hawk4j.example.core.config;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.yy.hawk4j.core.executor.DynamicThreadPool;
import com.yy.hawk4j.core.executor.SpringDynamicThreadPool;
import com.yy.hawk4j.core.executor.support.ThreadPoolBuilder;
import com.yy.hawk4j.example.core.handler.TaskTraceBuilderHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import static com.yy.hawk4j.example.core.constant.GlobalTestConstant.MESSAGE_CONSUME;
import static com.yy.hawk4j.example.core.constant.GlobalTestConstant.MESSAGE_PRODUCE;

@Slf4j
@Configuration
public class DynamicThreadPoolConfig {

    @Bean
    @DynamicThreadPool
    public Executor messageConsumeTtlDynamicThreadPool() {
        String threadPoolId = MESSAGE_CONSUME;
        ThreadPoolExecutor customExecutor = ThreadPoolBuilder.builder()
                .dynamicPool()
                .threadFactory(threadPoolId)
                .threadPoolId(threadPoolId)
                .executeTimeOut(800L)
                .waitForTasksToCompleteOnShutdown(true)
                .awaitTerminationMillis(5000L)
                .taskDecorator(new TaskTraceBuilderHandler())
                .build();
        // Ali ttl adaptation use case.
        Executor ttlExecutor = TtlExecutors.getTtlExecutor(customExecutor);
        return ttlExecutor;
    }

    @Bean
    @DynamicThreadPool
    public Executor testConsumeTtlDynamicThreadPool() {
        String threadPoolId = "test-consume";
        ThreadPoolExecutor customExecutor = ThreadPoolBuilder.builder()
                .dynamicPool()
                .threadFactory(threadPoolId)
                .threadPoolId(threadPoolId)
                .executeTimeOut(800L)
                .waitForTasksToCompleteOnShutdown(true)
                .awaitTerminationMillis(5000L)
                .taskDecorator(new TaskTraceBuilderHandler())
                .build();
        // Ali ttl adaptation use case.
        Executor ttlExecutor = TtlExecutors.getTtlExecutor(customExecutor);
        return ttlExecutor;
    }

    /**
     * {@link Bean @Bean} and {@link DynamicThreadPool @DynamicThreadPool}.
     */
    @SpringDynamicThreadPool
    public ThreadPoolExecutor messageProduceDynamicThreadPool() {
        return ThreadPoolBuilder.buildDynamicPoolById(MESSAGE_PRODUCE);
    }

}

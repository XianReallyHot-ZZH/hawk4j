package com.yy.hawk4j.core.executor;

import org.springframework.context.annotation.Bean;

import java.lang.annotation.*;

/**
 * A convenience annotation that is itself annotated with
 * {@link Bean @Bean} and {@link DynamicThreadPool @DynamicThreadPool}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Bean
@DynamicThreadPool
public @interface SpringDynamicThreadPool {
}

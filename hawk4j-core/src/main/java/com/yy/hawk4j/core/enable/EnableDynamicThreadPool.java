package com.yy.hawk4j.core.enable;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableDynamicThreadPool {
}

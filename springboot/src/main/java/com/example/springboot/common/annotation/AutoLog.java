package com.example.springboot.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
// 自动日志
public @interface AutoLog {
    String value() default "";
}

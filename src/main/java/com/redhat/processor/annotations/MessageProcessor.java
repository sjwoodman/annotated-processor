package com.redhat.processor.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a class that can act as a stream processor
 * @author hhiden
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageProcessor {
    SourceType configSource() default SourceType.ENVIRONMENT;
    String serverName() default "localhost";
    String port() default "9902";
}
package com.redhat.processor.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines an object that is an event source. It has annotations describing
 * where to connect to to send data.
 * @author hhiden
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageSource {
    SourceType configSource() default SourceType.ENVIRONMENT;
    String serverName() default "localhost";
    String port() default "9902";    
}

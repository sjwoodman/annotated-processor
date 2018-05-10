package com.redhat.processor.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates the method that gets started to create messages. At the moment, this
 * is called once and a producer should just start a Thread.
 * @author hhiden
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProduceMessage {
    public SourceType configSource() default SourceType.ENVIRONMENT;
    String outputClientId() default "producer-id";
    public String outputName() default "";
    public String interval() default "1000";
}
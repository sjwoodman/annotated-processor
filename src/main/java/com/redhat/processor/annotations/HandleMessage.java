package com.redhat.processor.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Labels method that will handle a message
 * @author hhiden
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HandleMessage {   
    public OutputType outputType() default OutputType.NONE;
    public SourceType configSource() default SourceType.ENVIRONMENT;
    String inputGroupName() default "processor-group";
    String outputClientId() default "producer-id";
    public String outputName() default "";
    public String inputName() default "";
}
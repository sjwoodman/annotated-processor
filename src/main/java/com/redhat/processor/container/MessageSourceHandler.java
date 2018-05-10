package com.redhat.processor.container;

import com.redhat.processor.annotations.HandleMessage;
import java.lang.reflect.Method;
import com.redhat.processor.annotations.ProduceMessage;

/**
 * Wrapper for a method that can create messages
 * @author hhiden
 */
public abstract class MessageSourceHandler implements Runnable {
    protected Object handler;
    protected Method handlerMethod;
    protected MessageSourceHandlerContainer parent;
    protected ProduceMessage config;
    
    public MessageSourceHandler(Object handler, Method handlerMethod, MessageSourceHandlerContainer parent, ProduceMessage config) {
        this.handler = handler;
        this.handlerMethod = handlerMethod;
        this.parent = parent;
        this.config = config;
    }
    
    public abstract void shutdown();
    
}
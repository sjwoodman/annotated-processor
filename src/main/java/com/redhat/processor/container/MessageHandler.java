package com.redhat.processor.container;

import com.redhat.processor.annotations.HandleMessage;
import java.lang.reflect.Method;

/**
 * This abstract class defines a handler that manages the passing of event data
 * to a method and the retrieval of results for propagation to a new stream
 * @author hhiden
 */
public abstract class MessageHandler implements Runnable {
    protected Object handler;
    protected Method handlerMethod;    
    protected MessageHandlerContainer parent;
    protected HandleMessage config;
    
    public MessageHandler(Object handler, Method handlerMethod, MessageHandlerContainer parent, HandleMessage config) {
        this.handler = handler;
        this.handlerMethod = handlerMethod;
        this.parent = parent;
        this.config = config;
    }
    
    public abstract void shutdown();
    
}

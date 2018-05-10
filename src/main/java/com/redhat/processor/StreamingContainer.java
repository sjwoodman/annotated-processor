package com.redhat.processor;

/**
 * This class contains a class that can participate in a stream application. It
 * can hold a source, processor or a sink.
 * @author hhiden
 */
public abstract class StreamingContainer {
    protected Object processorObject;
    protected String serverName;
    protected int serverPort;    

    public StreamingContainer(Object processorObject) {
        this.processorObject = processorObject;
    }

    public String getServerName() {
        return serverName;
    }

    public Object getProcessorObject() {
        return processorObject;
    }

    public int getServerPort() {
        return serverPort;
    }
    
    
    public abstract void start();
    public abstract void shutdown();
}
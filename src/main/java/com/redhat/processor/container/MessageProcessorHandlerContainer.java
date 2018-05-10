package com.redhat.processor.container;

import com.redhat.processor.StreamingContainer;
import com.redhat.processor.container.kafka.KafkaMessageProcessorHandler;
import com.redhat.processor.annotations.HandleMessage;
import com.redhat.processor.annotations.MessageProcessor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Holds a message processor and finds queues etc
 * @author hhiden
 */
public class MessageProcessorHandlerContainer extends StreamingContainer {
    private static final Logger logger = Logger.getLogger(MessageProcessorHandlerContainer.class.getName());
    
    
    private HashMap<String, MessageProcessorHandler> handlerMap = new HashMap<>();

    public MessageProcessorHandlerContainer(Object processorObject) {
        super(processorObject);
        configureProcessor();
    }
    
    private void configureProcessor(){
        if(this.processorObject!=null){
            logger.info("Configuring processor: " + this.processorObject.getClass().getName());
            // Messaging system setup
            Class pClass = processorObject.getClass();
            Annotation[] annotations = pClass.getAnnotationsByType(MessageProcessor.class);
            if(annotations.length==1){
                MessageProcessor mpa = (MessageProcessor)annotations[0];
                serverName = ContainerUtils.resolve(mpa.configSource(), mpa.serverName());
                serverPort = Integer.parseInt(ContainerUtils.resolve(mpa.configSource(), mpa.port()));
                logger.info("Configured messaging service: " + serverName + ":" + serverPort);
                        
            } else {
                serverName = "localhost";
                serverPort = 9092;
                logger.warning("Using defaults for messaging service");
            }
            
            // Find the processor message handler
            Method[] methods = pClass.getDeclaredMethods();
            for(Method m : methods){
                if(m.getAnnotation(HandleMessage.class)!=null){
                    logger.info("Found handler method: " + m.getName());                    
                    HandleMessage hma = (HandleMessage)m.getAnnotation(HandleMessage.class);
                    KafkaMessageProcessorHandler handler = new KafkaMessageProcessorHandler(this, processorObject, m, hma);
                    handlerMap.put(hma.inputName(), handler);
                }
            }
        }
    }

    /**
     * Connect queues and start to receive messages
     */
    @Override
    public void start(){
        for(MessageProcessorHandler h : handlerMap.values()){
            new Thread(h).start();
        }
    }
    
    /**
     * Disconnect everything and stop messages
     */
    @Override
    public void shutdown(){
        for(MessageProcessorHandler h : handlerMap.values()){
            h.shutdown();
        }
    }
}
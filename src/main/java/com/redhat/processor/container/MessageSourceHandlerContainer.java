package com.redhat.processor.container;

import com.redhat.processor.StreamingContainer;
import com.redhat.processor.annotations.HandleMessage;
import com.redhat.processor.annotations.MessageProcessor;
import com.redhat.processor.annotations.MessageSource;
import com.redhat.processor.container.kafka.KafkaMessageSourceHandler;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.logging.Logger;
import com.redhat.processor.annotations.ProduceMessage;

/**
 * Provides a container for a message source
 * @author hhiden
 */
public class MessageSourceHandlerContainer extends StreamingContainer {
    private static final Logger logger = Logger.getLogger(MessageSourceHandlerContainer.class.getName());
    private HashMap<String, MessageSourceHandler> handlerMap = new HashMap<>();
    
    public MessageSourceHandlerContainer(Object processorObject) {
        super(processorObject);
        configureSource();
    }

    private void configureSource(){
        if(processorObject!=null){
            logger.info("Configuring message source for object: " + processorObject.getClass().getName());
            // Messaging system setup
            Class pClass = processorObject.getClass();
            Annotation[] annotations = pClass.getAnnotationsByType(MessageSource.class);
            if(annotations.length==1){
                MessageSource msa = (MessageSource)annotations[0];
                serverName = ContainerUtils.resolve(msa.configSource(), msa.serverName());
                serverPort = Integer.parseInt(ContainerUtils.resolve(msa.configSource(), msa.port()));
                logger.info("Configured messaging service: " + serverName + ":" + serverPort);
                        
            } else {
                serverName = "localhost";
                serverPort = 9092;
                logger.warning("Using defaults for messaging service");
            }        
            
            // Populate parameters
            ContainerUtils.populateFields(processorObject);
            
            // Source methods
            Method[] methods = pClass.getDeclaredMethods();
            for(Method m : methods){
                if(m.getAnnotation(ProduceMessage.class)!=null){
                    logger.info("Found handler method: " + m.getName());                    
                    ProduceMessage hma = (ProduceMessage)m.getAnnotation(ProduceMessage.class);
                    KafkaMessageSourceHandler handler = new KafkaMessageSourceHandler(processorObject, m, this, hma);
                    handlerMap.put(hma.outputName(), handler);
                }
            } 
        }
    }
    
    /**
     * Connect queues and start to send messages
     */
    @Override
    public void start(){
        for(MessageSourceHandler h : handlerMap.values()){
            new Thread(h).start();
        }
    }
    
    /**
     * Disconnect everything and stop messages
     */
    @Override
    public void shutdown(){
        for(MessageSourceHandler h : handlerMap.values()){
            h.shutdown();
        }
    }
}
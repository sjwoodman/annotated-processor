package com.redhat.processor.container;

import com.redhat.processor.annotations.MessageProcessor;
import eu.infomas.annotation.AnnotationDetector;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides an entry point that scans for all of the message processor
 * objects in the classpath and starts them all up.
 * @author hhiden
 */
public class MessageHandlerApplication {
    private static final Logger logger = Logger.getLogger(MessageHandlerApplication.class.getName());
    
    private ArrayList<MessageHandlerContainer> containers = new ArrayList<>();

    public MessageHandlerApplication() {
        
    }
    
    public void start(){
        findAndInstantiateMessageProcessors();
        startMessageHandlerContainers();
    }
    
    public void shutdown(){
        stopMessageHandlerContainers();
    }
    
    private void findAndInstantiateMessageProcessors(){
        logger.info("Scanning for MessageProcessors...");
        try {
            AnnotationDetector.TypeReporter reporter = new AnnotationDetector.TypeReporter() {
                @Override
                public void reportTypeAnnotation(Class<? extends Annotation> annotation, String className) {
                    logger.info("Found processor class: " + className);
                    try {
                        Class processorClass = Class.forName(className);
                        final Object processor = processorClass.newInstance();
                        logger.info("Instantiated: " + processor.getClass().getSimpleName());
                        final MessageHandlerContainer container = new MessageHandlerContainer(processor);
                        containers.add(container);
                    } catch (Exception e){
                        logger.log(Level.SEVERE, "Error creating processor class; " + e.getMessage(), e.getMessage());
                    }
                }

                @Override
                public Class<? extends Annotation>[] annotations() {
                    return new Class[]{MessageProcessor.class};
                }
            };
            AnnotationDetector detector = new AnnotationDetector(reporter);
            detector.detect();
        } catch (Exception e){
            e.printStackTrace();
        }        
    }
    
    private void startMessageHandlerContainers(){
        logger.info("Starting containers");
        for(MessageHandlerContainer c : containers){
            c.start();
        }
    }
    
    private void stopMessageHandlerContainers(){
        for(MessageHandlerContainer c : containers){
            c.shutdown();
        }
    }
    
    public static void main(String[] args){
        MessageHandlerApplication app = new MessageHandlerApplication();
        app.start();
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            app.shutdown();
            System.out.println("SHUTDOWN");
        }));          
    }
}

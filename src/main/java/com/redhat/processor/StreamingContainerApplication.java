package com.redhat.processor;

import com.redhat.processor.annotations.MessageProcessor;
import com.redhat.processor.annotations.MessageSink;
import com.redhat.processor.annotations.MessageSource;
import com.redhat.processor.container.MessageProcessorHandlerContainer;
import com.redhat.processor.container.MessageSourceHandlerContainer;
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
public class StreamingContainerApplication {
    private static final Logger logger = Logger.getLogger(StreamingContainerApplication.class.getName());
    
    private ArrayList<StreamingContainer> containers = new ArrayList<>();

    public StreamingContainerApplication() {
        
    }
    
    public void start(){
        findAndInstantiateMessageProcessors();
        startMessageHandlerContainers();
    }
    
    public void shutdown(){
        stopMessageHandlerContainers();
    }
    
    private void findAndInstantiateMessageProcessors(){
        logger.info("Scanning for MessagingComponents...");
        try {
            AnnotationDetector.TypeReporter reporter = new AnnotationDetector.TypeReporter() {
                @Override
                public void reportTypeAnnotation(Class<? extends Annotation> annotation, String className) {
                    logger.info("Found processor class: " + className);
                    if(annotation.equals(MessageProcessor.class)){
                        // Create a message processor object
                        try {
                            Class processorClass = Class.forName(className);
                            final Object processor = processorClass.newInstance();
                            logger.info("Instantiated: " + processor.getClass().getSimpleName());
                            final MessageProcessorHandlerContainer container = new MessageProcessorHandlerContainer(processor);
                            containers.add(container);
                        } catch (Exception e){
                            logger.log(Level.SEVERE, "Error creating processor class: " + e.getMessage(), e.getMessage());
                        }
                    } else if(annotation.equals(MessageSource.class)){
                        // Create a scheduled source object
                        try {
                            Class sourceClass = Class.forName(className);
                            final Object source = sourceClass.newInstance();
                            logger.info("Instantiated: " + source.getClass().getSimpleName());
                            final MessageSourceHandlerContainer container = new MessageSourceHandlerContainer(source);
                            containers.add(container);
                            
                        } catch (Exception e){
                            logger.log(Level.SEVERE, "Error creating source class: " + e.getMessage(), e);
                        }
                    }
                }

                @Override
                public Class<? extends Annotation>[] annotations() {
                    return new Class[]{MessageProcessor.class, MessageSource.class, MessageSink.class};
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
        for(StreamingContainer c : containers){
            c.start();
        }
    }
    
    private void stopMessageHandlerContainers(){
        for(StreamingContainer c : containers){
            c.shutdown();
        }
    }
    
    public static void main(String[] args){
        StreamingContainerApplication app = new StreamingContainerApplication();
        app.start();
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            app.shutdown();
            System.out.println("SHUTDOWN");
        }));          
    }
}

package com.redhat.processor.container.kafka;

import com.redhat.processor.annotations.OutputType;
import com.redhat.processor.container.ContainerUtils;
import com.redhat.processor.container.MessageSourceHandler;
import com.redhat.processor.container.MessageSourceHandlerContainer;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.logging.Logger;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.LongSerializer;
import com.redhat.processor.annotations.ProduceMessage;
import java.util.logging.Level;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * Kafka implementation of a message source handler
 * @author hhiden
 */
public class KafkaMessageSourceHandler extends MessageSourceHandler {
    private static final Logger logger = Logger.getLogger(KafkaMessageSourceHandler.class.getName());
    
    private String outputStreamName;
    private String outputClientId;
    private int interval;
    
    private volatile boolean shutdownFlag = false;
    private Producer<Long, byte[]> outputProducer;
    
    public KafkaMessageSourceHandler(Object handler, Method handlerMethod, MessageSourceHandlerContainer parent, ProduceMessage config) {
        super(handler, handlerMethod, parent, config);
        outputStreamName = ContainerUtils.resolve(config.configSource(), config.outputName());
        outputClientId = config.outputClientId();
        interval = Integer.parseInt(ContainerUtils.resolve(config.configSource(), config.interval()));
        
        logger.info("Configured message producer on stream: " + outputStreamName);
   
    }

    /** Create a Kafka producer for a queue */
    private Producer<Long, byte[]> createProducer(String groupName) {
        logger.info("Creating Kafka producer with ClientID: " + groupName);
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, parent.getServerName() + ":" + parent.getServerPort());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, groupName);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        
        return new KafkaProducer<>(props);
    }       
    @Override
    public void shutdown() {
        shutdownFlag = true;
        if (outputProducer != null) {
            outputProducer.close();
        }
    }

    @Override
    public void run() {
        outputProducer = createProducer(outputClientId);

        while(!shutdownFlag){
            try {
                 final Object result = handlerMethod.invoke(handler);
                 if(result!=null){
                    byte[] returnByteData = ContainerUtils.serialize(result);
                    ProducerRecord<Long, byte[]> outputRecord = new ProducerRecord<>(outputStreamName, System.nanoTime(), returnByteData);
                    RecordMetadata metadata = outputProducer.send(outputRecord).get();                 
                 }
            } catch (Exception e){
                logger.log(Level.SEVERE, "Error running producer method: " + e.getMessage(), e);
            }            
            try {
                Thread.sleep(interval);
            } catch (InterruptedException ie){
                logger.info("Interrupted");
                shutdownFlag = true;
            }
        }
    }
}
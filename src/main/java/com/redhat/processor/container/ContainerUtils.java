
package com.redhat.processor.container;

import com.redhat.processor.annotations.SourceType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 *
 * @author hhiden
 */
public class ContainerUtils {
    public static final Object deserialize(byte[] data) throws Exception {
        try(ByteArrayInputStream stream = new ByteArrayInputStream(data)){
            try (ObjectInputStream obStream = new ObjectInputStream(stream)){
                return obStream.readObject();
            }
        }
    }
    
    public static final byte[] serialize(Object object) throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try (ObjectOutputStream obStream = new ObjectOutputStream(stream)){
            obStream.writeObject(object);
        }
        stream.flush();
        stream.close();
        return stream.toByteArray();
    }
    
    public static String resolve(final SourceType sourceType, final String variable) {
        if(sourceType==SourceType.ENVIRONMENT){
            String value = System.getProperty(variable);
            if (value == null) {
                // than we try ENV ...
                value = System.getenv(variable);
            }
            return value;
        } else {
            return variable;
        }
    }    
}


package com.redhat.processor.container;

import com.redhat.processor.annotations.ServiceParameter;
import com.redhat.processor.annotations.SourceType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.logging.Logger;

/**
 *
 * @author hhiden
 */
public class ContainerUtils {
    private static final Logger logger = Logger.getLogger(ContainerUtils.class.getName());
    
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
    
    public static void populateFields(Object targetObject){
        Field[] fields = targetObject.getClass().getDeclaredFields();
        for(Field f : fields){
            Annotation a = f.getAnnotation(ServiceParameter.class);
            if(a!=null){
                String value = resolve(SourceType.ENVIRONMENT, ((ServiceParameter)a).name());
                if(value!=null){
                    try {
                        f.setAccessible(true);
                        switch(f.getType().getName()){
                            case "int":
                                f.setInt(targetObject, Integer.parseInt(value));
                                break;
                                
                            case "java.lang.Integer":
                                f.set(targetObject, new Integer(value));
                                break;
                                
                            case "long":
                                f.setLong(targetObject, Long.parseLong(value));
                                break;
                                
                            case "java.lang.Long":
                                f.set(targetObject, new Long(value));
                                break;
                                
                            case "double":
                                f.setDouble(targetObject, Double.parseDouble(value));
                                break;
                                
                            case "java.lang.Double":
                                f.set(targetObject, new Double(value));
                                break;
                                
                            case "boolean":
                                f.setBoolean(targetObject, Boolean.parseBoolean(value));
                                break;
                                
                            case "java.lang.Boolean":
                                f.set(targetObject, new Boolean(value));
                                break;
                                
                            case "float":
                                f.setFloat(targetObject, Float.parseFloat(value));
                                break;
                                
                            case "java.lang.Float":
                                f.set(targetObject, new Float(value));
                                break;
                                
                            default:
                                f.set(targetObject, value);

                        }
                        
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                    
                }
            }
        }
    }
    
}

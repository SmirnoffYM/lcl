package com.habds.lcl.core.processor;

/**
 * Exception occurred when it is not possible to perform actual object transformation
 * during execution of {@link Processor#process(Object, Class)}
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 12/1/2015 12:29 AM
 */
public class LinkProcessingException extends RuntimeException {

    private Object entity;
    private Class<?> dtoClass;

    public LinkProcessingException(Object entity, Class<?> dtoClass) {
        this(entity, dtoClass, null);
    }

    public LinkProcessingException(Object entity, Class<?> dtoClass, Throwable cause) {
        super("Unable to perform link processing for " + dtoClass + ". Entity: " + entity, cause);
        this.entity = entity;
        this.dtoClass = dtoClass;
    }

    public Object getEntity() {
        return entity;
    }

    public Class<?> getDtoClass() {
        return dtoClass;
    }
}

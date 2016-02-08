package com.habds.lcl.core.processor;

/**
 * Exception occurred when it is not possible to set up linking mappings for specified class.
 * For example: when {@link com.habds.lcl.core.annotation.ClassLink} annotation is missing.
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 12/1/2015 12:31 AM
 */
public class LinkEstablishingException extends RuntimeException {

    private Class<?> dtoClass;

    public LinkEstablishingException(Class<?> dtoClass, Throwable cause) {
        super("Unable to perform link establishing for " + dtoClass, cause);
        this.dtoClass = dtoClass;
    }

    public Class<?> getDtoClass() {
        return dtoClass;
    }
}

package com.habds.lcl.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation marks field to be linked with specified Entity's property.
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 11/30/2015 11:13 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Link {

    /**
     * Reference to linked property.
     * Syntax depends on {@link com.habds.lcl.core.processor.LinkProcessor} implementations.
     *
     * @return reference to linked property
     */
    String value() default "";
}

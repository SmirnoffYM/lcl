package com.habds.lcl.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation specifies the desired type of elements of the collection field
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 12/14/15 12:04 AM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Contains {

    /**
     * Desired type of elements for the collection field
     *
     * @return type of collection elements
     */
    Class<?> value() default Object.class;
}

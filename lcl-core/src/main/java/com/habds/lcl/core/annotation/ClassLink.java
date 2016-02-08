package com.habds.lcl.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation marks DTO class to be linkable by {@link com.habds.lcl.core.processor.Processor}
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 11/30/2015 11:11 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ClassLink {

    /**
     * Link root
     *
     * @return Entity class to be linked to
     */
    Class<?> value();
}

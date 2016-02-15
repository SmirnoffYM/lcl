package com.habds.lcl.core.data.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark DTO's field with this annotation to add "greaterThan" or "greaterThanOrEqualTo" filter
 * during call of {@link com.habds.lcl.core.processor.Processor#createSpecs(Object)}
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 2/14/16 4:21 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface From {

    boolean exclusive() default false;
}

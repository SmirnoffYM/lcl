package com.habds.lcl.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation marks field to be completely ignored by {@link com.habds.lcl.core.processor.Processor}
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 2/14/16 1:06 AM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Ignored {
}

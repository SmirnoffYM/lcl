package com.habds.lcl.core.data.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark DTO's field with this annotation to negate any filtering
 *
 * @author Yurii Smyrnov
 * @version 1
 * @see Filter#negate()
 * @since 8/21/16 2:57 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Not {
}

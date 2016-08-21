package com.habds.lcl.core.data.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark DTO's field with this annotation to add "isNotNull" filter
 * during call of {@link com.habds.lcl.core.processor.Processor#createSpecs(Object)}.
 * Applicable only to boolean or Boolean fields. Equivalent to using {@link IsNull} + {@link Not}
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 8/21/16 2:57 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IsNotNull {
}

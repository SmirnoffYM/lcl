package com.habds.lcl.core.data.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark DTO's field with this annotation to add "isNull" or "isNotNull" filter
 * during call of {@link com.habds.lcl.core.processor.Processor#createSpecs(Object)}.
 * Applicable only to boolean or Boolean fields.
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 2/16/16 4:38 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IsNull {
}

package com.habds.lcl.core.processor.impl;

import com.habds.lcl.core.processor.SetterMapping;
import com.habds.lcl.core.processor.impl.util.Property;

import java.lang.reflect.Field;

/**
 * Transformation that can applied to value that is going to be set into Entity's field.
 * Applicability is determined by
 * {@link SetterPostMapping#isApplicable(String, Property, Class, Field, SetterPostMappingChain)} method.
 * {@code dtoField} and {@code dtoPropertyClass} in both methods can be nullable.
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 3/21/16 3:08 PM
 */
public interface SetterPostMapping {

    boolean isApplicable(String remainingPath, Property entityProperty,
                         Class dtoPropertyClass, Field dtoField, SetterPostMappingChain chain);

    SetterMapping getMapping(String remainingPath, Property entityProperty,
                             Class dtoPropertyClass, Field dtoField, SetterPostMappingChain chain);
}

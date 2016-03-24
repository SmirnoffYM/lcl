package com.habds.lcl.core.processor.impl;

import com.habds.lcl.core.processor.GetterMapping;

import java.lang.reflect.Field;

/**
 * Transformation that can applied to value extracted from Entity's field just before
 * getting it's property (next in the chain) or setting it into DTO's field.
 * Applicability is determined by
 * {@link GetterPostMapping#isApplicable(String, Class, Class, Field, GetterPostMappingChain)} method.
 * {@code dtoField} in both methods can be nullable.
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 2/2/16 9:17 PM
 */
public interface GetterPostMapping {

    boolean isApplicable(String remainingPath, Class entityPropertyClass,
                         Class dtoPropertyClass, Field dtoField, GetterPostMappingChain chain);

    GetterMapping getMapping(String remainingPath, Class entityPropertyClass,
                             Class dtoPropertyClass, Field dtoField, GetterPostMappingChain chain);
}

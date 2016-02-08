package com.habds.lcl.core.processor.impl;

import com.habds.lcl.core.processor.GetterMapping;
import com.habds.lcl.core.processor.Processor;

import java.lang.reflect.Field;

/**
 * Transformation that can applied to value extracted from Entity's field just before setting it into DTO's field.
 * Applicability is determined by {@link PostMapping#isApplicable(Class, Class, Field, Processor)} method.
 * {@code dtoField} in both cases can be nullable.
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 2/2/16 9:17 PM
 */
public interface PostMapping {

    boolean isApplicable(Class entityPropertyClass, Class dtoPropertyClass, Field dtoField, Processor processor);

    GetterMapping getMapping(Class entityPropertyClass, Class dtoPropertyClass, Field dtoField, PostMappingChain chain);
}

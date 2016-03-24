package com.habds.lcl.core.processor.impl;

import com.habds.lcl.core.processor.Processor;
import com.habds.lcl.core.processor.SetterMapping;

import java.lang.reflect.Field;

/**
 * Chain of {@link SetterPostMapping}s. Allows to start postmapping recursively.
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 3/21/16 3:09 PM
 * @see SetterPostMapping
 */
public interface SetterPostMappingChain {

    /**
     * Launch execution of the postmapping chain
     *
     * @param remainingPath    {@link com.habds.lcl.core.annotation.Link} remaining path
     * @param entityClass      Entity class
     * @param dtoPropertyClass DTO's property class (may be null)
     * @param dtoField         DTO's property field (may be null)
     * @return setter mapping function
     */
    SetterMapping setterMapping(String remainingPath, Class entityClass, Class dtoPropertyClass, Field dtoField);

    Processor getProcessor();
}

package com.habds.lcl.core.processor.impl;

import com.habds.lcl.core.processor.GetterMapping;
import com.habds.lcl.core.processor.Processor;

import java.lang.reflect.Field;

/**
 * Chain of {@link GetterPostMapping}s. Allows to start postmapping recursively (for collections&amp;arrays support etc)
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 2/2/16 10:33 PM
 */
public interface GetterPostMappingChain {

    /**
     * Launch execution of the postmapping chain
     *
     * @param remainingPath    {@link com.habds.lcl.core.annotation.Link} remaining path
     * @param entityClass      Entity class
     * @param dtoPropertyClass DTO's property class
     * @param dtoField         DTO's property field (may be null)
     * @return getter mapping function
     */
    GetterMapping getterMapping(String remainingPath, Class entityClass, Class dtoPropertyClass, Field dtoField);

    Processor getProcessor();
}

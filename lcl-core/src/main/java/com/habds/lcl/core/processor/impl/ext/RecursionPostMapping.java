package com.habds.lcl.core.processor.impl.ext;

import com.habds.lcl.core.processor.GetterMapping;
import com.habds.lcl.core.processor.Processor;
import com.habds.lcl.core.processor.impl.PostMapping;
import com.habds.lcl.core.processor.impl.PostMappingChain;

import java.lang.reflect.Field;

/**
 * {@link PostMapping} designed to support propagation for fields that are of type marked with
 * {@link com.habds.lcl.core.annotation.ClassLink}
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 2/2/16 9:51 PM
 */
public class RecursionPostMapping implements PostMapping {

    @Override
    public boolean isApplicable(Class entityPropertyClass, Class dtoPropertyClass, Field dtoField,
                                Processor processor) {
        return processor.isProcessable(dtoPropertyClass);
    }

    @Override
    public GetterMapping getMapping(Class entityPropertyClass, Class dtoPropertyClass, Field dtoField,
                                    PostMappingChain chain) {
        return (entityProperty, dto) -> chain.getProcessor().process(entityProperty, dtoPropertyClass);
    }
}

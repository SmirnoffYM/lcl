package com.habds.lcl.core.processor.impl.ext;

import com.habds.lcl.core.processor.GetterMapping;
import com.habds.lcl.core.processor.impl.GetterPostMapping;
import com.habds.lcl.core.processor.impl.GetterPostMappingChain;

import java.lang.reflect.Field;

/**
 * {@link GetterPostMapping} designed to support propagation for fields that are of type marked with
 * {@link com.habds.lcl.core.annotation.ClassLink}
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 2/2/16 9:51 PM
 */
public class RecursionGetterPostMapping implements GetterPostMapping {

    @Override
    public boolean isApplicable(String remainingPath, Class entityPropertyClass, Class dtoPropertyClass, Field dtoField,
                                GetterPostMappingChain chain) {
        return chain.getProcessor().isProcessable(dtoPropertyClass);
    }

    @Override
    public GetterMapping getMapping(String remainingPath, Class entityPropertyClass,
                                    Class dtoPropertyClass, Field dtoField,
                                    GetterPostMappingChain chain) {
        return (entityProperty, dto) -> chain.getProcessor().process(entityProperty, dtoPropertyClass);
    }
}

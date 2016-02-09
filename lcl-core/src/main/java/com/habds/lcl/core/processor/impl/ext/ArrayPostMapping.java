package com.habds.lcl.core.processor.impl.ext;

import com.habds.lcl.core.processor.GetterMapping;
import com.habds.lcl.core.processor.impl.PostMapping;
import com.habds.lcl.core.processor.impl.PostMappingChain;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * {@link PostMapping} for array fields
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 2/2/16 11:01 PM
 */
public class ArrayPostMapping implements PostMapping {

    @Override
    public boolean isApplicable(String remainingPath, Class entityPropertyClass, Class dtoPropertyClass,
                                Field dtoField, PostMappingChain chain) {
        return entityPropertyClass.isArray() && dtoPropertyClass.isArray();
    }

    @Override
    public GetterMapping getMapping(String remainingPath,
                                    Class entityPropertyClass, Class dtoPropertyClass, Field dtoField,
                                    PostMappingChain chain) {
        return (entityProperty, dto) -> {
            Object[] value = (Object[]) entityProperty;
            Object[] targetProperty = (Object[]) Array.newInstance(dtoPropertyClass, value.length);
            for (int i = 0; i < value.length; i++) {
                targetProperty[i] = chain
                    .getterMapping(remainingPath, value[i].getClass(), dtoPropertyClass.getComponentType(), null)
                    .map(value[i], dto);
            }
            return targetProperty;
        };
    }
}

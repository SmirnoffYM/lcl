package com.habds.lcl.core.processor.impl.ext;

import com.habds.lcl.core.processor.GetterMapping;
import com.habds.lcl.core.processor.SetterMapping;
import com.habds.lcl.core.processor.impl.*;
import com.habds.lcl.core.processor.impl.util.Property;

import java.lang.reflect.Field;

/**
 * Class allows enum conversions to string, integer or other enums during building
 * {@link SetterMapping} and {@link GetterMapping}
 *
 * @author Yurii Smyrnov
 * @version 2
 * @since 2/2/16 10:27 PM
 */
@SuppressWarnings("unchecked")
public class EnumPostMapping implements GetterPostMapping, SetterPostMapping {

    @Override
    public boolean isApplicable(String remainingPath, Class entityPropertyClass, Class dtoPropertyClass, Field dtoField,
                                GetterPostMappingChain chain) {
        return convertible(entityPropertyClass, dtoPropertyClass) || convertible(dtoPropertyClass, entityPropertyClass);
    }

    @Override
    public boolean isApplicable(String remainingPath, Property entityProperty, Class dtoPropertyClass, Field dtoField,
                                SetterPostMappingChain chain) {
        Class entityPropertyClass = entityProperty.getType();
        return entityPropertyClass.isEnum()
            && (dtoPropertyClass == null || dtoPropertyClass.isEnum()
            || dtoPropertyClass == String.class || Number.class.isAssignableFrom(dtoPropertyClass))
            || dtoPropertyClass != null && dtoPropertyClass.isEnum()
            && (entityPropertyClass == String.class
            || Number.class.isAssignableFrom(entityPropertyClass) || entityPropertyClass.isEnum());
    }

    private boolean convertible(Class from, Class to) {
        return from.isEnum() && (to.isEnum() || to == String.class || Number.class.isAssignableFrom(to));
    }

    @Override
    public GetterMapping getMapping(String remainingPath, Class entityPropertyClass, Class dtoPropertyClass,
                                    Field dtoField, GetterPostMappingChain chain) {
        if (dtoPropertyClass.isEnum()) {
            return (entityProperty, dto) -> SimpleConverter.toEnum(dtoPropertyClass, entityProperty);
        }

        if (entityPropertyClass.isEnum() && dtoPropertyClass == String.class) {
            return (property, dto) -> property == null ? null : ((Enum) property).name();
        } else {
            return (property, dto) -> property == null ? null : ((Integer) ((Enum) property).ordinal());
        }
    }

    @Override
    public SetterMapping getMapping(String remainingPath, Property entityProperty, Class dtoPropertyClass,
                                    Field dtoField, SetterPostMappingChain chain) {
        Class entityPropertyClass = entityProperty.getType();
        return (entity, dtoProperty) -> entityProperty.setter()
            .apply(entity, SimpleConverter.toEnum(entityPropertyClass, dtoProperty));
    }
}

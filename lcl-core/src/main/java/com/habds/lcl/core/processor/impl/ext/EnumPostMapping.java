package com.habds.lcl.core.processor.impl.ext;

import com.habds.lcl.core.processor.GetterMapping;
import com.habds.lcl.core.processor.impl.PostMapping;
import com.habds.lcl.core.processor.impl.PostMappingChain;

import java.lang.reflect.Field;

/**
 * {@link PostMapping} for conversion between entity and dto enum values.
 * Applicable if either entity property OR dto property are enums.
 * <ul>
 * <li>If both are same enums, mapping function will return incoming value</li>
 * <li>If both are different enums, try to make transformation calling
 * {@link Enum#valueOf(Class, String)} with name of the incoming enum as second argument</li>
 * <li>If dto prop is enum, return entity enum's name</li>
 * <li>If entity prop is string, return {@link Enum#valueOf(Class, String)}</li>
 * <li>If entity prop is enum, return its ordinal</li>
 * <li>Else return element from {@link Class#getEnumConstants()}</li>
 * </ul>
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 2/2/16 10:27 PM
 */
@SuppressWarnings("unchecked")
public class EnumPostMapping implements PostMapping {

    @Override
    public boolean isApplicable(String remainingPath, Class entityPropertyClass, Class dtoPropertyClass, Field dtoField,
                                PostMappingChain chain) {
        return convertible(entityPropertyClass, dtoPropertyClass) || convertible(dtoPropertyClass, entityPropertyClass);
    }

    private boolean convertible(Class from, Class to) {
        return from.isEnum() && (to.isEnum() || to == String.class || Number.class.isAssignableFrom(to));
    }

    @Override
    public GetterMapping getMapping(String remainingPath, Class entityPropertyClass, Class dtoPropertyClass,
                                    Field dtoField, PostMappingChain chain) {
        if (dtoPropertyClass.equals(entityPropertyClass)) {
            return (property, dto) -> property;
        } else if (dtoPropertyClass.isEnum() && entityPropertyClass.isEnum()) {
            return (property, dto) -> property == null
                ? null : Enum.valueOf((Class<Enum>) dtoPropertyClass, ((Enum) property).name());
        }

        if (entityPropertyClass.isEnum() && dtoPropertyClass == String.class) {
            return (property, dto) -> property == null ? null : ((Enum) property).name();
        } else if (entityPropertyClass == String.class && dtoPropertyClass.isEnum()) {
            return (property, dto) -> property == null ? null :
                Enum.valueOf((Class<Enum>) dtoPropertyClass, (String) property);
        } else if (entityPropertyClass.isEnum()) {
            return (property, dto) -> property == null ? null :
                ((Integer) ((Enum) property).ordinal());
        } else {
            return (property, dto) -> property == null ? null :
                (dtoPropertyClass.getEnumConstants()[((Number) property).intValue()]);
        }
    }
}

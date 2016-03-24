package com.habds.lcl.core.processor;

/**
 * Property getter mapping function, can be chained using {@link GetterMapping#andThen(GetterMapping)} method.
 * Used for retrieval (an with possible additional transformation) of Entity's property value, that
 * later could be set into DTO's property.
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 12/14/15 12:27 AM
 */
@FunctionalInterface
public interface GetterMapping {

    /**
     * Map source property value from source entity into target property value from target entity
     *
     * @param entityProperty entity's property value
     * @param dto            DTO
     * @return DTO's property value
     */
    Object map(Object entityProperty, Object dto);

    default GetterMapping andThen(GetterMapping after) {
        return (s, t) -> after.map(map(s, t), t);
    }
}

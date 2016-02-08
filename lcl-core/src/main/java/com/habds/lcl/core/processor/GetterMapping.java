package com.habds.lcl.core.processor;

/**
 * Property getter mapping function, can be chained using {@link this#andThen(GetterMapping)} method.
 * Used for retrieval (an with possible additional transformation) of source property value from source entity, that
 * later will be set into target property of target entity.
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
     * @param sourceProperty source property value
     * @param targetEntity   target entity
     * @return target property value
     */
    Object map(Object sourceProperty, Object targetEntity);

    default GetterMapping andThen(GetterMapping after) {
        return (s, t) -> after.map(map(s, t), t);
    }
}

package com.habds.lcl.core.processor;

/**
 * Property setter mapping function. Used for setting target property value from target entity into source property
 * from source entity. Walking through property chain may be customized: additional transformations or value changing
 * can be applied.
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 2/6/16 11:07 PM
 */
@FunctionalInterface
public interface SetterMapping {

    /**
     * Set up target value into source entity
     *
     * @param sourceEntity source entity
     * @param targetValue  target value
     * @return target value that will be set up
     */
    Object map(Object sourceEntity, Object targetValue);
}

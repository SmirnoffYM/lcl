package com.habds.lcl.core.processor;

/**
 * Property setter mapping function. Used for setting DTO's property value into Entity.
 * Walking through property chain may be customized: additional transformations or value changing
 * can be applied.
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 2/6/16 11:07 PM
 */
@FunctionalInterface
public interface SetterMapping {

    /**
     * Set up DTO's property value into specified Entity
     *
     * @param entity      Entity
     * @param dtoProperty DTO's property value
     * @return value that was actually set up
     */
    Object map(Object entity, Object dtoProperty);
}

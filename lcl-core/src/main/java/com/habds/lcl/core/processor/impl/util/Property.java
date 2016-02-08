package com.habds.lcl.core.processor.impl.util;

import java.lang.reflect.Field;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Property of an object. Contains reference to appropriate field, getter and setter functions.
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 12/1/2015 11:50 PM
 */
@SuppressWarnings("unchecked")
public class Property<T, P> {

    private Field field;
    private String name;
    private Function<T, P> getter;
    private BiFunction<T, P, P> setter;

    public Property(Class<T> clazz, Field field) {
        if (field == null) {
            throw new IllegalArgumentException("Invalid field for class " + clazz);
        }
        name = field.getName();
        this.field = field;

        getter = o -> {
            try {
                return (P) this.field.get(o);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        };
        setter = (o, p) -> {
            ClassCache.getInstance().setPropertyValue(o, name, p);
            return p;
        };
    }

    public Function<T, P> getter() {
        return getter;
    }

    public BiFunction<T, P, P> setter() {
        return setter;
    }

    public String getName() {
        return name;
    }

    public Class<P> getType() {
        return (Class<P>) field.getType();
    }

    public Field getField() {
        return field;
    }
}

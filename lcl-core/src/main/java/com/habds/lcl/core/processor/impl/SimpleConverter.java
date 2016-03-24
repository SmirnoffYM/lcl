package com.habds.lcl.core.processor.impl;

import com.habds.lcl.core.data.filter.Converter;

import javax.persistence.criteria.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Common {@link Converter} implementation. Supports Date and Enum conversions.
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 2/1/16 1:10 AM
 */
@SuppressWarnings("unchecked")
public class SimpleConverter extends Converter {

    public SimpleConverter(Path entityPropertyPath) {
        super(entityPropertyPath);
    }

    @Override
    public Object convert(Object value) {
        Class type = entityPropertyPath.getJavaType();
        if (type.isEnum()) {
            return toEnum((Class<Enum>) type, value);
        }
        if (type == Date.class) {
            return toDate(value);
        }
        return value;
    }

    public static Date toDate(Object o) {
        if (o.getClass() == int.class || o.getClass() == long.class) {
            return new Date((long) o);
        } else if (o instanceof Number) {
            return new Date(((Number) o).longValue());
        } else if (o instanceof String) {
            return Date.from(Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse((String) o)));
        }
        return (Date) o;
    }

    public static Enum toEnum(Class<Enum> enumClass, Object o) {
        if (o == null) {
            return null;
        }
        if (enumClass == o.getClass()) {
            return (Enum) o;
        } else if (o instanceof Integer || o.getClass() == int.class) {
            return enumClass.getEnumConstants()[(int) o];
        } else if (o instanceof Long || o.getClass() == long.class) {
            return enumClass.getEnumConstants()[(int) (long) o];
        } else if (o instanceof String) {
            return Enum.valueOf(enumClass, (String) o);
        }
        throw new IllegalArgumentException("Type " + o.getClass() + " is not supported for Enum conversions");
    }
}

package com.habds.lcl.core.data.filter.impl;

import com.habds.lcl.core.data.filter.Converter;
import com.habds.lcl.core.data.filter.Filter;

import javax.persistence.criteria.*;

/**
 * Equality filter
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 1/3/16 6:23 PM
 */
public class Equals extends Filter<Object> {

    private Object value;

    public Equals() {
    }

    public Equals(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public <R> Predicate buildPredicate(Path<Object> path, Root<R> root, CriteriaQuery<?> query, CriteriaBuilder cb,
                                        Converter converter) {
        return cb.equal(path, converter.convert(value));
    }
}

package com.habds.lcl.core.data.filter.impl;

import com.habds.lcl.core.data.filter.Converter;
import com.habds.lcl.core.data.filter.Filter;

import javax.persistence.criteria.*;

/**
 * SQL LIKE filter
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 1/3/16 10:38 PM
 */
public class Like extends Filter<String> {

    private String value;

    public Like() {
    }

    public Like(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public <R> Predicate buildPredicate(Path<String> path, Root<R> root, CriteriaQuery<?> query, CriteriaBuilder cb,
                                        Converter converter) {
        return cb.like(cb.lower(path), "%" + value.toLowerCase() + "%");
    }
}

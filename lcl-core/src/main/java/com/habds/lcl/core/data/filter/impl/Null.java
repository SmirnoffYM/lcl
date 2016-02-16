package com.habds.lcl.core.data.filter.impl;

import com.habds.lcl.core.data.filter.Converter;
import com.habds.lcl.core.data.filter.Filter;

import javax.persistence.criteria.*;

/**
 * SQL IS NULL/IS NOT NULL filter
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 2/16/16 4:42 PM
 */
public class Null extends Filter<Object> {

    public Null() {
    }

    public Null(boolean isNull) {
        negated = !isNull;
    }

    @Override
    protected <R> Predicate buildPredicate(Path<Object> path, Root<R> root, CriteriaQuery<?> q, CriteriaBuilder cb,
                                           Converter converter) {
        return path.isNull();
    }
}

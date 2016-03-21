package com.habds.lcl.core.data.filter.impl;

import com.habds.lcl.core.data.filter.Converter;
import com.habds.lcl.core.data.filter.Filter;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SQL IN filter. If specified list of values is empty, it will be ignored.
 *
 * @author Yurii Smyrnov
 * @version 2
 * @since 1/3/16 11:12 PM
 */
public class In extends Filter<Object> {

    private List<Object> values;

    public In() {
    }

    public In(Collection<Object> values) {
        this.values = new ArrayList<>(values);
    }

    public In(Object... values) {
        this.values = Arrays.asList(values);
    }

    public List<Object> getValues() {
        return values;
    }

    public void setValues(List<Object> values) {
        this.values = values;
    }

    @Override
    public <R> Predicate buildPredicate(Path<Object> path, Root<R> root, CriteriaQuery<?> query, CriteriaBuilder cb,
                                        Converter converter) {
        return values.isEmpty()
            ? cb.and()
            : path.in(values.stream().map(converter::convert).collect(Collectors.toList()));
    }
}

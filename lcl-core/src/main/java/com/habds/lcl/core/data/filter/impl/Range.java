package com.habds.lcl.core.data.filter.impl;

import com.habds.lcl.core.data.filter.Converter;
import com.habds.lcl.core.data.filter.Filter;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Range filter
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 1/3/16 6:16 PM
 */
public class Range<S, T extends Comparable> extends Filter<T> {

    private S from;
    private S fromExclusive;
    private S to;
    private S toExclusive;

    public Range() {
    }

    public Range(S from, S to) {
        this.from = from;
        this.to = to;
    }

    public Range<S, T> from(S from) {
        setFrom(from);
        return this;
    }

    public Range<S, T> fromExclusive(S fromExclusive) {
        setFromExclusive(fromExclusive);
        return this;
    }

    public Range<S, T> to(S to) {
        setTo(to);
        return this;
    }

    public Range<S, T> toExclusive(S toExclusive) {
        setToExclusive(toExclusive);
        return this;
    }

    public S getFrom() {
        return from;
    }

    public void setFrom(S from) {
        if (from != null && fromExclusive != null) {
            throw new IllegalArgumentException("Please select from OR fromExclusive");
        }
        this.from = from;
    }

    public S getFromExclusive() {
        return fromExclusive;
    }

    public void setFromExclusive(S fromExclusive) {
        if (from != null && fromExclusive != null) {
            throw new IllegalArgumentException("Please select from OR fromExclusive");
        }
        this.fromExclusive = fromExclusive;
    }

    public S getTo() {
        return to;
    }

    public void setTo(S to) {
        if (to != null && toExclusive != null) {
            throw new IllegalArgumentException("Please select to OR toExclusive");
        }
        this.to = to;
    }

    public S getToExclusive() {
        return toExclusive;
    }

    public void setToExclusive(S toExclusive) {
        if (to != null && toExclusive != null) {
            throw new IllegalArgumentException("Please select to OR toExclusive");
        }
        this.toExclusive = toExclusive;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> Predicate buildPredicate(Path<T> path, Root<R> root, CriteriaQuery<?> query, CriteriaBuilder cb,
                                        Converter converter) {
        List<Predicate> predicates = new ArrayList<>();
        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(path, (T) converter.convert(from)));
        }
        if (to != null) {
            predicates.add(cb.lessThanOrEqualTo(path, (T) converter.convert(to)));
        }
        if (fromExclusive != null) {
            predicates.add(cb.greaterThan(path, (T) converter.convert(fromExclusive)));
        }
        if (toExclusive != null) {
            predicates.add(cb.lessThan(path, (T) converter.convert(toExclusive)));
        }
        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
    }
}

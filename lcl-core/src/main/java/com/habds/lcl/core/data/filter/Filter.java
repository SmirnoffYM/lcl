package com.habds.lcl.core.data.filter;

import javax.persistence.criteria.*;
import java.io.Serializable;

/**
 * Interface represents single part (predicate) of SQL WHERE clause.
 * Multiple filters will be composed using AND operator.
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 1/3/16 6:15 PM
 */
public abstract class Filter<P> implements Serializable {

    protected boolean negated = false;

    /**
     * Negate the filter predicate - NOT operator
     *
     * @return itself
     */
    public Filter<P> negate() {
        negated = !negated;
        return this;
    }

    public boolean isNegated() {
        return negated;
    }

    public void setNegated(boolean negated) {
        this.negated = negated;
    }

    /**
     * Build filtering predicate using specified property path and root/query/criteriabuilder objects
     *
     * @param path      property JPA path
     * @param root      query root
     * @param q         query
     * @param cb        criteria builder
     * @param converter class performs filter parameters conversion
     * @param <R>       type of query root
     * @return filtering predicate
     */
    protected abstract <R> Predicate buildPredicate(Path<P> path, Root<R> root, CriteriaQuery<?> q, CriteriaBuilder cb,
                                                    Converter converter);

    public <R> Predicate getPredicate(Path<P> path, Root<R> root, CriteriaQuery<?> q, CriteriaBuilder cb,
                                      Converter converter) {
        Predicate predicate = buildPredicate(path, root, q, cb, converter);
        return negated ? predicate.not() : predicate;
    }
}

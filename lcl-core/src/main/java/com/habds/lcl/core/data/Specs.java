package com.habds.lcl.core.data;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Filtering specification of the Entity.
 * Has the same purpose as spring-data's Specification class,
 * created in order to simply not to add one extra dependency.
 *
 * @param <T> type of entity to be filtered
 * @author Yurii Smyrnov
 * @version 1
 * @since 12/17/15 9:43 AM
 */
@FunctionalInterface
public interface Specs<T> {

    Predicate buildPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb);
}

package com.habds.lcl.core.data;

import com.habds.lcl.core.data.filter.Filter;
import com.habds.lcl.core.processor.Processor;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper class provides simple convenient methods to retrieve linked entities via {@link EntityManager}
 * using specified filters, sortings etc.
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 1/4/16 10:38 PM
 */
public class EntityManagerRepository {

    private Processor processor;
    private EntityManager em;

    public <S, T> List<T> getAll(Map<String, Filter> filters, Class<T> targetClass) {
        return getAll(filters, new PagingAndSorting(), targetClass);
    }

    public <S, T> List<T> getAll(Map<String, Filter> filters, PagingAndSorting pagingAndSorting, Class<T> targetClass) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        Class<S> sourceClass = processor.getLink(targetClass);
        Specs<S> specs = processor.createSpecs(filters, targetClass);

        CriteriaQuery<S> criteriaQuery = cb.createQuery(sourceClass);
        Root<S> root = criteriaQuery.from(sourceClass);
        criteriaQuery.where(specs.buildPredicate(root, criteriaQuery, cb));

        List<Order> orders = pagingAndSorting.getSortings().entrySet().stream()
            .map(e -> {
                Path<?> path = processor.getJpaPath(targetClass, e.getKey(), root, criteriaQuery, cb);
                return e.getValue() ? cb.asc(path) : cb.desc(path);
            }).collect(Collectors.toList());
        criteriaQuery.orderBy(orders);

        TypedQuery<S> query = em.createQuery(criteriaQuery);
        if (pagingAndSorting.getPage() != null) {
            query.setFirstResult(pagingAndSorting.getPage() * pagingAndSorting.getPageSize());
        }
        if (pagingAndSorting.getPageSize() != null) {
            query.setMaxResults(pagingAndSorting.getPageSize());
        }
        List<S> result = query.getResultList();
        return result.stream().map(s -> processor.process(s, targetClass)).collect(Collectors.toList());
    }

    public <T> long count(Map<String, Filter> filters, Class<T> targetClass) {
        return createCountQuery(filters, targetClass).getSingleResult();
    }

    public <S, T> T getOne(Map<String, Filter> filters, Class<T> targetClass) {
        try {
            return processor.process(this.<S, T>createQuery(filters, targetClass).getSingleResult(), targetClass);
        } catch (NoResultException ex) {
            return null;
        }
    }

    public <T> boolean exists(Map<String, Filter> filters, Class<T> targetClass) {
        return count(filters, targetClass) > 0;
    }

    private <S, T> TypedQuery<S> createQuery(Map<String, Filter> filters, Class<T> targetClass) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        Class<S> sourceClass = processor.getLink(targetClass);
        Specs<S> specs = processor.createSpecs(filters, targetClass);

        CriteriaQuery<S> query = cb.createQuery(sourceClass);
        Root<S> root = query.from(sourceClass);
        query.where(specs.buildPredicate(root, query, cb));
        return em.createQuery(query);
    }

    private <S, T> TypedQuery<Long> createCountQuery(Map<String, Filter> filters, Class<T> targetClass) {
        Class<S> sourceClass = processor.getLink(targetClass);
        Specs<S> specs = processor.createSpecs(filters, targetClass);

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<S> root = query.from(sourceClass);
        query.select(cb.count(root));
        query.where(specs.buildPredicate(root, query, cb));
        return em.createQuery(query);
    }

    public EntityManagerRepository() {
    }

    public EntityManagerRepository(Processor processor, EntityManager em) {
        this.processor = processor;
        this.em = em;
    }

    public Processor getProcessor() {
        return processor;
    }

    public void setProcessor(Processor processor) {
        this.processor = processor;
    }

    public EntityManager getEm() {
        return em;
    }

    public void setEm(EntityManager em) {
        this.em = em;
    }
}

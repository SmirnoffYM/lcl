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

    protected Processor processor;
    protected EntityManager em;

    public <DTO> List<DTO> getAll(Map<String, Filter> filters, Class<DTO> dtoClass) {
        return getAll(filters, new PagingAndSorting(), dtoClass);
    }

    public <ENTITY, DTO> List<ENTITY> getAll(DTO dto) {
        return getAll(dto, new PagingAndSorting());
    }

    public <DTO> List<DTO> getAll(Map<String, Filter> filters, PagingAndSorting pagingAndSorting, Class<DTO> dtoClass) {
        return processor.process(
            getAll(processor.createSpecs(filters, dtoClass), pagingAndSorting, dtoClass),
            dtoClass);
    }

    public <ENTITY, DTO> List<ENTITY> getAll(DTO dto, PagingAndSorting pagingAndSorting) {
        return getAll(processor.createSpecs(dto), pagingAndSorting, dto.getClass());
    }

    public <ENTITY, DTO> List<ENTITY> getAll(Specs<ENTITY> specs, PagingAndSorting pagingAndSorting,
                                             Class<DTO> dtoClass) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        Class<ENTITY> entityClass = processor.getLink(dtoClass);

        CriteriaQuery<ENTITY> criteriaQuery = cb.createQuery(entityClass);
        Root<ENTITY> root = criteriaQuery.from(entityClass);
        criteriaQuery.where(specs.buildPredicate(root, criteriaQuery, cb));

        List<Order> orders = pagingAndSorting.getSortings().entrySet().stream()
            .map(e -> {
                Path<?> path = processor.getJpaPath(dtoClass, e.getKey(), root, criteriaQuery, cb);
                return e.getValue() ? cb.asc(path) : cb.desc(path);
            }).collect(Collectors.toList());
        criteriaQuery.orderBy(orders);

        TypedQuery<ENTITY> query = em.createQuery(criteriaQuery);
        if (pagingAndSorting.getPage() != null) {
            query.setFirstResult(pagingAndSorting.getPage() * pagingAndSorting.getPageSize());
        }
        if (pagingAndSorting.getPageSize() != null) {
            query.setMaxResults(pagingAndSorting.getPageSize());
        }
        return query.getResultList();
    }

    public <DTO> long count(Map<String, Filter> filters, Class<DTO> dtoClass) {
        return createCountQuery(processor.createSpecs(filters, dtoClass), dtoClass).getSingleResult();
    }

    public <DTO> long count(DTO dto) {
        return createCountQuery(processor.createSpecs(dto), dto.getClass()).getSingleResult();
    }

    public <DTO> DTO getOne(Map<String, Filter> filters, Class<DTO> dtoClass) {
        try {
            return processor.process(
                createQuery(processor.createSpecs(filters, dtoClass), dtoClass).getSingleResult(),
                dtoClass);
        } catch (NoResultException ex) {
            return null;
        }
    }

    public <ENTITY, DTO> ENTITY getOne(DTO dto) {
        try {
            return createQuery(processor.<ENTITY, DTO>createSpecs(dto), dto.getClass()).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public <DTO> boolean exists(Map<String, Filter> filters, Class<DTO> dtoClass) {
        return count(filters, dtoClass) > 0;
    }

    public <DTO> boolean exists(DTO dto) {
        return count(dto) > 0;
    }

    private <ENTITY, DTO> TypedQuery<ENTITY> createQuery(Specs<ENTITY> specs, Class<DTO> dtoClass) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        Class<ENTITY> entityClass = processor.getLink(dtoClass);

        CriteriaQuery<ENTITY> query = cb.createQuery(entityClass);
        Root<ENTITY> root = query.from(entityClass);
        query.where(specs.buildPredicate(root, query, cb));
        return em.createQuery(query);
    }

    private <ENTITY, DTO> TypedQuery<Long> createCountQuery(Specs<ENTITY> specs, Class<DTO> dtoClass) {
        Class<ENTITY> entityClass = processor.getLink(dtoClass);

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ENTITY> root = query.from(entityClass);
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

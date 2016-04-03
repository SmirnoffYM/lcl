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
 * @version 2
 * @since 1/4/16 10:38 PM
 */
public class EntityManagerRepository {

    protected Processor processor;
    protected EntityManager em;

    /**
     * Get all Entity records by specified filters map and DTO class
     *
     * @param filters  filters map
     * @param dtoClass DTO class
     * @param <DTO>    type of DTO
     * @return list of Entities
     * @see Processor#createSpecs(Map, Class)
     */
    public <DTO> List<DTO> getAll(Map<String, Filter> filters, Class<DTO> dtoClass) {
        return getAll(processor.createSpecs(filters, dtoClass), new PagingAndSorting(), dtoClass, false);
    }

    /**
     * Get all distinct (eliminating duplicates) Entity records by specified filters map and DTO class
     *
     * @param filters  filters map
     * @param dtoClass DTO class
     * @param <DTO>    type of DTO
     * @return list of distinct Entities
     * @see Processor#createSpecs(Map, Class)
     */
    public <DTO> List<DTO> getAllDistinct(Map<String, Filter> filters, Class<DTO> dtoClass) {
        return getAll(processor.createSpecs(filters, dtoClass), new PagingAndSorting(), dtoClass, true);
    }

    /**
     * Get all Entity records by specified filtering DTO
     *
     * @param dto      filtering DTO
     * @param <ENTITY> type of Entity
     * @param <DTO>    type of DTO
     * @return list of Entities
     */
    public <ENTITY, DTO> List<ENTITY> getAll(DTO dto) {
        return getAll(processor.createSpecs(dto), new PagingAndSorting(), dto.getClass(), false);
    }

    /**
     * Get all distinct (eliminating duplicates) Entity records by specified filtering DTO
     *
     * @param dto      filtering DTO
     * @param <ENTITY> type of Entity
     * @param <DTO>    type of DTO
     * @return list of distinct Entities
     */
    public <ENTITY, DTO> List<ENTITY> getAllDistinct(DTO dto) {
        return getAll(processor.createSpecs(dto), new PagingAndSorting(), dto.getClass(), true);
    }

    /**
     * Get paged results converted from Entities to DTOs by specified filters map and DTO class
     *
     * @param filters          filters map
     * @param pagingAndSorting pagination and sorting settings
     * @param dtoClass         DTO class
     * @param <ENTITY>         type of Entity
     * @param <DTO>            type of DTO
     * @return page of Entities converted into DTOs
     */
    public <ENTITY, DTO> Sheet<DTO> getAll(Map<String, Filter> filters, PagingAndSorting pagingAndSorting,
                                           Class<DTO> dtoClass) {
        Specs<ENTITY> specs = processor.createSpecs(filters, dtoClass);
        List<DTO> content = processor.process(getAll(specs, pagingAndSorting, dtoClass, false), dtoClass);
        return new Sheet<>(content, createCountQuery(specs, dtoClass, false).getSingleResult(), pagingAndSorting);
    }

    /**
     * Get distinct paged results converted from Entities to DTOs by specified filters map and DTO class
     *
     * @param filters          filters map
     * @param pagingAndSorting pagination and sorting settings
     * @param dtoClass         DTO class
     * @param <ENTITY>         type of Entity
     * @param <DTO>            type of DTO
     * @return page of distinct Entities converted into DTOs
     */
    public <ENTITY, DTO> Sheet<DTO> getAllDistinct(Map<String, Filter> filters, PagingAndSorting pagingAndSorting,
                                                   Class<DTO> dtoClass) {
        Specs<ENTITY> specs = processor.createSpecs(filters, dtoClass);
        List<DTO> content = processor.process(getAll(specs, pagingAndSorting, dtoClass, true), dtoClass);
        return new Sheet<>(content, createCountQuery(specs, dtoClass, true).getSingleResult(), pagingAndSorting);
    }

    /**
     * Get paged list of Entities by specified filtering DTO
     *
     * @param dto              DTO
     * @param pagingAndSorting pagination and sorting settings
     * @param <ENTITY>         type of Entity
     * @param <DTO>            type of DTO
     * @return page of Entities
     */
    public <ENTITY, DTO> Sheet<ENTITY> getAll(DTO dto, PagingAndSorting pagingAndSorting) {
        Specs<ENTITY> specs = processor.createSpecs(dto);
        List<ENTITY> content = getAll(specs, pagingAndSorting, dto.getClass(), false);
        return new Sheet<>(content, createCountQuery(specs, dto.getClass(), false).getSingleResult(), pagingAndSorting);
    }

    /**
     * Get paged list of distinct Entities by specified filtering DTO
     *
     * @param dto              DTO
     * @param pagingAndSorting pagination and sorting settings
     * @param <ENTITY>         type of Entity
     * @param <DTO>            type of DTO
     * @return page of distinct Entities
     */
    public <ENTITY, DTO> Sheet<ENTITY> getAllDistinct(DTO dto, PagingAndSorting pagingAndSorting) {
        Specs<ENTITY> specs = processor.createSpecs(dto);
        List<ENTITY> content = getAll(specs, pagingAndSorting, dto.getClass(), true);
        return new Sheet<>(content, createCountQuery(specs, dto.getClass(), true).getSingleResult(), pagingAndSorting);
    }

    private <ENTITY, DTO> List<ENTITY> getAll(Specs<ENTITY> specs, PagingAndSorting pagingAndSorting,
                                              Class<DTO> dtoClass, boolean distinct) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        Class<ENTITY> entityClass = processor.getLink(dtoClass);

        CriteriaQuery<ENTITY> criteriaQuery = cb.createQuery(entityClass);
        Root<ENTITY> root = criteriaQuery.from(entityClass);
        criteriaQuery.where(specs.buildPredicate(root, criteriaQuery, cb));
        criteriaQuery.distinct(distinct);

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

    /**
     * Count Entity records by specified filters map and DTO class
     *
     * @param filters  filters map
     * @param dtoClass DTO class
     * @param <DTO>    type of DTO
     * @return number of Entities
     * @see Processor#createSpecs(Map, Class)
     */
    public <DTO> long count(Map<String, Filter> filters, Class<DTO> dtoClass) {
        return createCountQuery(processor.createSpecs(filters, dtoClass), dtoClass, false).getSingleResult();
    }

    /**
     * Count distinct Entity records by specified filters map and DTO class
     *
     * @param filters  filters map
     * @param dtoClass DTO class
     * @param <DTO>    type of DTO
     * @return number of distinct Entities
     * @see Processor#createSpecs(Map, Class)
     */
    public <DTO> long countDistinct(Map<String, Filter> filters, Class<DTO> dtoClass) {
        return createCountQuery(processor.createSpecs(filters, dtoClass), dtoClass, true).getSingleResult();
    }

    /**
     * Count Entities by specified filtering DTO
     *
     * @param dto   DTO
     * @param <DTO> type of DTO
     * @return number of Entities
     */
    public <DTO> long count(DTO dto) {
        return createCountQuery(processor.createSpecs(dto), dto.getClass(), false).getSingleResult();
    }

    /**
     * Count distinct Entities by specified filtering DTO
     *
     * @param dto   DTO
     * @param <DTO> type of DTO
     * @return number of distinct Entities
     */
    public <DTO> long countDistinct(DTO dto) {
        return createCountQuery(processor.createSpecs(dto), dto.getClass(), true).getSingleResult();
    }

    /**
     * Get single distinct Entity converted into DTO by specified filters map and DTO class
     *
     * @param filters  filters map
     * @param dtoClass DTO class
     * @param <DTO>    type of DTO
     * @return Entity converted into DTO or null if there are no such records in DB
     * @see Processor#createSpecs(Map, Class)
     */
    public <DTO> DTO getOne(Map<String, Filter> filters, Class<DTO> dtoClass) {
        try {
            return processor.process(
                createQuery(processor.createSpecs(filters, dtoClass), dtoClass, true).getSingleResult(),
                dtoClass);
        } catch (NoResultException ex) {
            return null;
        }
    }

    /**
     * Get single distinct Entity by specified filtering DTO
     *
     * @param dto      DTO
     * @param <ENTITY> type of Entity
     * @param <DTO>    type of DTO
     * @return Entity or null if there are no such records in DB
     */
    public <ENTITY, DTO> ENTITY getOne(DTO dto) {
        try {
            return createQuery(processor.<ENTITY, DTO>createSpecs(dto), dto.getClass(), true).getSingleResult();
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

    private <ENTITY, DTO> TypedQuery<ENTITY> createQuery(Specs<ENTITY> specs, Class<DTO> dtoClass, boolean distinct) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        Class<ENTITY> entityClass = processor.getLink(dtoClass);

        CriteriaQuery<ENTITY> query = cb.createQuery(entityClass);
        Root<ENTITY> root = query.from(entityClass);
        query.where(specs.buildPredicate(root, query, cb));
        query.distinct(distinct);
        return em.createQuery(query);
    }

    private <ENTITY, DTO> TypedQuery<Long> createCountQuery(Specs<ENTITY> specs, Class<DTO> dtoClass,
                                                            boolean distinct) {
        Class<ENTITY> entityClass = processor.getLink(dtoClass);

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ENTITY> root = query.from(entityClass);
        query.select(distinct ? cb.countDistinct(root) : cb.count(root));
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

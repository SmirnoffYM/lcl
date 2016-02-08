package com.habds.lcl.spring;

import com.habds.lcl.core.annotation.ClassLink;
import com.habds.lcl.core.data.Specs;
import com.habds.lcl.core.data.filter.Filter;
import com.habds.lcl.core.processor.impl.util.ClassCache;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * Helper class provides simple convenient methods to retrieve linked entities via {@link JpaSpecificationExecutor}
 * using specified filters, sortings etc.
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 1/31/16 12:34 PM
 */
@SuppressWarnings("unchecked")
public class JpaDao<ENTITY, DTO> {

    private SpringProcessor processor;
    private JpaSpecificationExecutor<ENTITY> delegate;
    private Class<DTO> dtoClass;

    JpaDao(SpringProcessor processor, JpaSpecificationExecutor<ENTITY> delegate, Class<DTO> dtoClass) {
        this.processor = processor;
        this.delegate = delegate;
        this.dtoClass = dtoClass;
    }

    public List<DTO> findAll(Map<String, Filter> filters) {
        Specs<ENTITY> specs = processor.createSpecs(filters, dtoClass);
        return delegate.findAll(specs::buildPredicate).stream()
            .map(s -> processor.process(s, dtoClass))
            .collect(toList());
    }

    public List<DTO> findAll(Map<String, Filter> filters, Sort sort) {
        Specs<ENTITY> specs = processor.createSpecs(filters, dtoClass);
        return delegate
            .findAll(specs::buildPredicate, convertToEntity(sort)).stream()
            .map(s -> processor.process(s, dtoClass))
            .collect(toList());
    }

    public Page<DTO> findAll(Map<String, Filter> filters, Pageable page) {
        Specs<ENTITY> specs = processor.createSpecs(filters, dtoClass);
        Page<DTO> result = delegate
            .findAll(specs::buildPredicate,
                new PageRequest(page.getPageNumber(), page.getPageSize(), convertToEntity(page.getSort())))
            .map(s -> processor.process(s, dtoClass));
        return overwriteSort(result, page);
    }

    private Sort convertToEntity(Sort sort) {
        if (sort == null) {
            return null;
        }
        return new Sort(StreamSupport.stream(sort.spliterator(), false).map(this::convertToEntity).collect(toList()));
    }

    private Page<DTO> overwriteSort(Page<DTO> result, Pageable page) {
        return new PageImpl<>(result.getContent(), page, result.getTotalElements());
    }

    private Sort.Order convertToEntity(Sort.Order order) {
        return new Sort.Order(order.getDirection(),
            processor.getPath(dtoClass, order.getProperty()),
            order.getNullHandling());
    }

    public DTO getOne(Map<String, Filter> filters) {
        Specs<ENTITY> specs = processor.createSpecs(filters, dtoClass);
        return processor.process(delegate.findOne(specs::buildPredicate), dtoClass);
    }

    public long count(Map<String, Filter> filters) {
        Specs<ENTITY> specs = processor.createSpecs(filters, dtoClass);
        return delegate.count(specs::buildPredicate);
    }

    public boolean exists(Map<String, Filter> filters) {
        return count(filters) > 0;
    }

    public ENTITY create(DTO dto) {
        return create(dto, new HashMap<>());
    }

    public ENTITY create(DTO dto, Map<String, ?> properties) {
        Class<ENTITY> entityClass = (Class<ENTITY>) dtoClass.getAnnotation(ClassLink.class).value();
        ENTITY entity = ClassCache.construct(entityClass);
        return update(entity, dto, properties);
    }

    public ENTITY createAndSave(DTO dto) {
        return repo().save(create(dto));
    }

    public ENTITY createAndSave(DTO dto, Map<String, ?> properties) {
        return repo().save(create(dto, properties));
    }

    public ENTITY update(ENTITY entity, DTO dto) {
        return processor.merge(entity, dto);
    }

    public ENTITY update(ENTITY entity, DTO dto, Map<String, ?> properties) {
        entity = processor.merge(entity, dto);
        return processor.merge(entity, properties);
    }

    public ENTITY updateAndSave(ENTITY entity, DTO dto) {
        return repo().save(update(entity, dto));
    }

    public ENTITY updateAndSave(ENTITY entity, DTO dto, Map<String, ?> properties) {
        return repo().save(update(entity, dto, properties));
    }

    public JpaRepository<ENTITY, ?> repo() {
        return (JpaRepository<ENTITY, ?>) delegate;
    }
}

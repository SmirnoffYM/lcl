package com.habds.lcl.spring;

import com.habds.lcl.core.data.Specs;
import com.habds.lcl.core.data.filter.Filter;
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
        return processor.process(delegate.findAll(specs::buildPredicate), dtoClass);
    }

    public List<ENTITY> findAll(DTO filters) {
        Specs<ENTITY> specs = processor.createSpecs(filters);
        return delegate.findAll(specs::buildPredicate);
    }

    public <T> List<T> findAll(DTO filters, Class<T> otherDtoClass) {
        return findAll(filters).stream().map(e -> processor.process(e, otherDtoClass)).collect(toList());
    }

    public List<DTO> findAll(Map<String, Filter> filters, Sort sort) {
        Specs<ENTITY> specs = processor.createSpecs(filters, dtoClass);
        return processor.process(delegate.findAll(specs::buildPredicate, convertToEntity(sort)), dtoClass);
    }

    public List<ENTITY> findAll(DTO filters, Sort sort) {
        Specs<ENTITY> specs = processor.createSpecs(filters);
        return delegate.findAll(specs::buildPredicate, convertToEntity(sort));
    }

    public Page<DTO> findAll(Map<String, Filter> filters, Pageable page) {
        Specs<ENTITY> specs = processor.createSpecs(filters, dtoClass);
        Page<DTO> result = delegate
            .findAll(specs::buildPredicate,
                new PageRequest(page.getPageNumber(), page.getPageSize(), convertToEntity(page.getSort())))
            .map(s -> processor.process(s, dtoClass));
        return overwriteSort(result, page);
    }

    public Page<ENTITY> findAll(DTO filters, Pageable page) {
        Specs<ENTITY> specs = processor.createSpecs(filters);
        return delegate.findAll(specs::buildPredicate, page == null
            ? null : new PageRequest(page.getPageNumber(), page.getPageSize(), convertToEntity(page.getSort())));
    }

    public <T> Page<T> findAll(DTO filters, Pageable page, Class<T> otherDtoClass) {
        Page<T> result = findAll(filters, page).map(e -> processor.process(e, otherDtoClass));
        return overwriteSort(result, page);
    }

    private Sort convertToEntity(Sort sort) {
        if (sort == null) {
            return null;
        }
        return new Sort(StreamSupport.stream(sort.spliterator(), false).map(this::convertToEntity).collect(toList()));
    }

    private <T> Page<T> overwriteSort(Page<T> result, Pageable page) {
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

    public ENTITY getOne(DTO filters) {
        Specs<ENTITY> specs = processor.createSpecs(filters);
        return delegate.findOne(specs::buildPredicate);
    }

    public long count(Map<String, Filter> filters) {
        Specs<ENTITY> specs = processor.createSpecs(filters, dtoClass);
        return delegate.count(specs::buildPredicate);
    }

    public long count(DTO filters) {
        return delegate.count(processor.<ENTITY, DTO>createSpecs(filters)::buildPredicate);
    }

    public boolean exists(Map<String, Filter> filters) {
        return count(filters) > 0;
    }

    public boolean exists(DTO filters) {
        return count(filters) > 0;
    }

    public ENTITY create(DTO dto) {
        return create(dto, new HashMap<>());
    }

    public ENTITY create(DTO dto, Map<String, ?> properties) {
        return processor.create(dto, properties);
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
        return processor.merge(entity, dto, properties);
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

package com.habds.lcl.core.processor.impl;

import com.habds.lcl.core.annotation.ClassLink;
import com.habds.lcl.core.data.Specs;
import com.habds.lcl.core.data.filter.Converter;
import com.habds.lcl.core.data.filter.Filter;
import com.habds.lcl.core.processor.LinkEstablishingException;
import com.habds.lcl.core.processor.LinkProcessingException;
import com.habds.lcl.core.processor.LinkProcessor;
import com.habds.lcl.core.processor.Processor;
import com.habds.lcl.core.processor.impl.util.ClassCache;

import javax.persistence.criteria.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Default processor implementation. Requires calling {@link SimpleProcessor#configure()}
 * each time new classes will be added.
 * Uses {@link ClassCache} for extracting and storing class (both Entity and DTO) fields at configuration phase.
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 12/1/2015 12:10 AM
 */
@SuppressWarnings("unchecked")
public class SimpleProcessor implements Processor {

    protected Map<Class, MappingMetadata> mappingMetadata = new HashMap<>();
    protected LinkProcessor linkProcessor;

    public SimpleProcessor() {
        this(new SimpleLinkProcessor());
    }

    public SimpleProcessor(LinkProcessor linkProcessor) {
        this.linkProcessor = linkProcessor;
    }

    /**
     * Add new DTO classes to metadata registry, perform basic checks
     *
     * @param dtos DTOs - classes marked with {@link ClassLink} annotation
     * @return this object
     */
    public SimpleProcessor add(Class... dtos) {
        for (Class<?> dto : dtos) {
            try {
                MappingMetadata mappingMetadata = new MappingMetadata<>(linkProcessor, dto);
                this.mappingMetadata.put(dto, mappingMetadata);
            } catch (Exception e) {
                throw new LinkEstablishingException(dto, e);
            }
        }
        return this;
    }

    /**
     * (Re-)create link mappings for each MappingMetadata entry
     *
     * @return this object
     * @see MappingMetadata#configure()
     */
    public SimpleProcessor configure() {
        linkProcessor.configure(this);
        mappingMetadata.values().forEach(metadata -> {
            try {
                metadata.configure();
            } catch (Exception e) {
                throw new LinkEstablishingException(metadata.getDtoClass(), e);
            }
        });
        return this;
    }

    @Override
    public <DTO> boolean isProcessable(Class<DTO> dtoClass) {
        return mappingMetadata.containsKey(dtoClass);
    }

    @Override
    public <ENTITY, DTO> Class<ENTITY> getLink(Class<DTO> dtoClass) {
        return mappingMetadata.get(dtoClass).getEntityClass();
    }

    @Override
    public <ENTITY, DTO> DTO process(ENTITY entity, Class<DTO> dtoClass) {
        if (entity == null) {
            return null;
        }
        MappingMetadata metadata = mappingMetadata.get(dtoClass);
        try {
            if (metadata == null) {
                throw new IllegalArgumentException("No mapping found for dto: " + dtoClass);
            }
            return (DTO) metadata.setUpDTO(entity);
        } catch (Exception e) {
            throw new LinkProcessingException(entity, dtoClass, e);
        }
    }

    @Override
    public <ENTITY, DTO> ENTITY merge(ENTITY entity, DTO dto) {
        if (entity == null) {
            return null;
        }
        MappingMetadata metadata = mappingMetadata.get(dto.getClass());
        try {
            if (metadata == null) {
                throw new IllegalArgumentException("No mapping found for dto: " + dto.getClass());
            }
            return (ENTITY) metadata.setUpEntity(entity, dto);
        } catch (Exception e) {
            throw new LinkProcessingException(entity, dto.getClass(), e);
        }
    }

    @Override
    public <ENTITY> ENTITY merge(ENTITY entity, Map<String, ?> properties) {
        properties.forEach((path, value) ->
            linkProcessor.setterMapping(path, entity.getClass(), null).map(entity, value));
        return entity;
    }

    @Override
    public <ENTITY, DTO> Specs<ENTITY> createSpecs(Map<String, Filter> filters, Class<DTO> dtoClass) {
        return (root, query, cb) ->
            cb.and(
                filters.entrySet().stream()
                    .map(e -> createPredicate(dtoClass, e.getKey(), e.getValue(), root, query, cb))
                    .toArray(Predicate[]::new)
            );
    }

    protected <ENTITY, DTO> Predicate createPredicate(Class<DTO> targetClass, String path, Filter filter,
                                                      Root root, CriteriaQuery query, CriteriaBuilder cb) {
        Path<ENTITY> jpaPath = getJpaPath(targetClass, path, root, query, cb);
        return filter.getPredicate(jpaPath, root, query, cb, createConverter(jpaPath));
    }

    protected Converter createConverter(Path sourcePath) {
        return new SimpleConverter(sourcePath);
    }

    @Override
    public <DTO> String getPath(Class<DTO> dtoClass, String propertyName) {
        return linkProcessor.getPath(dtoClass, propertyName);
    }

    @Override
    public <EP, DTO> Path<EP> getJpaPath(Class<DTO> dtoClass, String propertyName,
                                         Root root, CriteriaQuery query, CriteriaBuilder cb) {
        return linkProcessor.getJpaPath(dtoClass, propertyName, root, query, cb);
    }
}

package com.habds.lcl.core.processor.impl;

import com.habds.lcl.core.annotation.ClassLink;
import com.habds.lcl.core.annotation.Link;
import com.habds.lcl.core.processor.GetterMapping;
import com.habds.lcl.core.processor.LinkProcessor;
import com.habds.lcl.core.processor.SetterMapping;
import com.habds.lcl.core.processor.impl.util.ClassCache;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Metadata class, creates and holds all mapping functions for Entity and DTO class fields
 *
 * @param <S> type of Entity
 * @param <T> type of DTO
 */
@SuppressWarnings("unchecked")
public class MappingMetadata<S, T> {

    private LinkProcessor linkProcessor;
    private Class<S> entityClass;
    private Class<T> dtoClass;

    private Map<String, GetterMapping> getterMappers = new HashMap<>();
    private Map<String, SetterMapping> setterMappers = new HashMap<>();

    /**
     * Create metadata using specified DTO class, check if {@link ClassLink} annotation is present
     *
     * @param linkProcessor link processor
     * @param clazz         DTO class
     */
    public MappingMetadata(LinkProcessor linkProcessor, Class<T> clazz) {
        this.linkProcessor = linkProcessor;
        dtoClass = clazz;
        ClassLink classLink = dtoClass.getAnnotation(ClassLink.class);
        if (classLink == null) {
            throw new IllegalArgumentException("Class isn't annotated with @ClassLink: " + clazz);
        }
        entityClass = (Class<S>) classLink.value();

        ClassCache.getInstance().cacheFields(entityClass);
        ClassCache.getInstance().cacheFields(dtoClass);
    }

    /**
     * Create link mapping functions for all dto class fields
     */
    public void configure() {
        ClassCache.getInstance().getAllFields(dtoClass).forEach((name, field) -> {
            String path = toDotPath(field);
            getterMappers.put(name, linkProcessor.getterMapping(path, entityClass, field));
            setterMappers.put(name, linkProcessor.setterMapping(path, entityClass, field));
        });
    }

    /**
     * Get Entity's property dot-path from specified DTO's field
     *
     * @param dtoField field of DTO class
     * @return dot-path of Entity's property
     */
    public static String toDotPath(Field dtoField) {
        Link link = dtoField.getAnnotation(Link.class);
        String path = link == null ? null : link.value();
        if (path == null || path.isEmpty()) {
            path = dtoField.getName();
        }
        return path;
    }

    /**
     * Instantiate a dto and fill it with data using {@code getterMappers} and specified Entity
     *
     * @param entity Entity object, non-nullable
     * @return DTO
     */
    public T setUpDTO(S entity) {
        T dto = ClassCache.construct(this.dtoClass);
        getterMappers.forEach((k, v) -> ClassCache.getInstance().setPropertyValue(dto, k, v.map(entity, dto)));
        return dto;
    }

    /**
     * Fill specified Entity with data from DTO using {@code setterMappers}
     *
     * @param entity Entity
     * @param dto    DTO
     * @return Entity
     */
    public S setUpEntity(S entity, T dto) {
        setterMappers.forEach((k, v) -> v.map(entity,
            ClassCache.getInstance().getProperty((Class) dto.getClass(), k).getter().apply(dto)));
        return entity;
    }

    public Class<S> getEntityClass() {
        return entityClass;
    }

    public Class<T> getDtoClass() {
        return dtoClass;
    }
}

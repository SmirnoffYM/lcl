package com.habds.lcl.core.processor;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import java.lang.reflect.Field;

/**
 * Class creates mappers that process property references
 * and perform post-processing mapping (when Entity's property class differs from DTO's property class)
 *
 * @param <P> type of processor using this link processor
 * @author Yurii Smyrnov
 * @version 1
 * @since 12/1/2015 9:17 PM
 */
public interface LinkProcessor<P extends Processor> {

    /**
     * Set up specified processor to be used for handling nested DTOs etc
     *
     * @param processor processor
     */
    void configure(P processor);

    /**
     * Get property getter mapping function for specified DTO field and Entity class.
     *
     * @param path        property path specified in the {@link com.habds.lcl.core.annotation.Link} annotation
     *                    (or implicitly)
     * @param entityClass Entity class
     * @param dtoField    DTO field
     * @return property getter mapping function
     */
    GetterMapping getterMapping(String path, Class entityClass, Field dtoField);

    /**
     * Get property setter mapping function for specified DTO field and Entity class.
     *
     * @param path        property path specified in the {@link com.habds.lcl.core.annotation.Link} annotation
     *                    (or implicitly)
     * @param entityClass Entity class
     * @param dtoField    DTO field
     * @return property setter mapping function
     */
    SetterMapping setterMapping(String path, Class entityClass, Field dtoField);

    /**
     * Get dot-path for Entity's property by specified DTO class and DTO's property name
     *
     * @param dtoClass     DTO class
     * @param propertyName name of DTO's property
     * @param <T>          type of DTO
     * @return Entity's property path
     */
    <T> String getPath(Class<T> dtoClass, String propertyName);

    /**
     * Construct JPA path for Entity's property by specified DTO class and DTO's property
     *
     * @param dtoClass     class of DTO
     * @param propertyName name of DTO's property
     * @param beginning    starting point for JPA Path building, specify Root for the very beginning
     * @param query        JPA Criteria Query
     * @param cb           JPA Criteria Builder
     * @param <EP>         type of Entity's property
     * @param <DTO>        type of DTO
     * @return JPA Path object
     */
    <EP, DTO> Path<EP> getJpaPath(Class<DTO> dtoClass, String propertyName,
                                  Path beginning, CriteriaQuery query, CriteriaBuilder cb);
}

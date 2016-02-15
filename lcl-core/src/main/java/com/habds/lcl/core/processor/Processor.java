package com.habds.lcl.core.processor;

import com.habds.lcl.core.annotation.ClassLink;
import com.habds.lcl.core.data.Specs;
import com.habds.lcl.core.data.filter.Filter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class processes classes marked with {@link com.habds.lcl.core.annotation.ClassLink} annotation - DTOs.
 * Implementations must define what classes can be processed (this can be specified by package or explicit class list)
 * and what {@link LinkProcessor} will be used.
 * <p>
 * Here "DTO class" means class annotated with {@link com.habds.lcl.core.annotation.ClassLink} annotation
 * and "Entity class" - class specified in DTO's {@link ClassLink#value()}
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 11/30/2015 11:52 PM
 */
public interface Processor {

    /**
     * Check if DTO class is processable (is registered within Processor)
     *
     * @param dtoClass DTO class
     * @param <DTO>    type of DTO
     * @return true if specified DTO class can be processed using {@link Processor#process(Object, Class)} call
     */
    <DTO> boolean isProcessable(Class<DTO> dtoClass);

    /**
     * Return Entity class by specified DTO class
     *
     * @param dtoClass DTO class
     * @param <ENTITY> type of Entity
     * @param <DTO>    type of DTO
     * @return Entity class
     */
    <ENTITY, DTO> Class<ENTITY> getLink(Class<DTO> dtoClass);

    /**
     * Create DTO from the specified Entity.
     *
     * @param entity   Entity
     * @param dtoClass class of DTO
     * @param <ENTITY> type of Entity
     * @param <DTO>    type of DTO
     * @return DTO object
     */
    <ENTITY, DTO> DTO process(ENTITY entity, Class<DTO> dtoClass);

    default <ENTITY, DTO> List<DTO> process(List<ENTITY> entities, Class<DTO> dtoClass) {
        return entities.stream().map(e -> process(e, dtoClass)).collect(Collectors.toList());
    }

    /**
     * Merge data from DTO into Entity
     *
     * @param entity   Entity
     * @param dto      DTO
     * @param <ENTITY> type of Entity
     * @param <DTO>    type of DTO
     * @return updated Entity
     */
    <ENTITY, DTO> ENTITY merge(ENTITY entity, DTO dto);

    /**
     * Merge data from dot-path properties map into Entity
     *
     * @param entity     Entity
     * @param properties dto-path properties map
     * @param <ENTITY>   type of Entity
     * @return updated Entity
     */
    <ENTITY> ENTITY merge(ENTITY entity, Map<String, ?> properties);

    /**
     * Create JPA specification for Entity based on specified DTO and filters
     *
     * @param filters  filters: keys are names of properties of DTO class, values are actual {@link Filter} objects
     * @param dtoClass DTO class
     * @param <ENTITY> type of Entity
     * @param <DTO>    type of DTO
     * @return JPA specification for filtering Entities
     */
    <ENTITY, DTO> Specs<ENTITY> createSpecs(Map<String, Filter> filters, Class<DTO> dtoClass);

    /**
     * Create JPA specification for Entity using filtering DTO, that will be transformed into
     * filters map, see first argument in {@link Processor#createSpecs(Map, Class)} method.
     * Any DTO's field holding {@code null} value or marked with
     * {@link com.habds.lcl.core.annotation.Ignored} annotation will not be included into map.
     *
     * Additionally, filtering DTO can implement {@link Specs} interface itself, then the result of this method will be
     * composition of filters map predicate and
     * predicate built using {@link Specs#buildPredicate(Root, CriteriaQuery, CriteriaBuilder)} method.
     *
     * @param dto      filtering DTO
     * @param <ENTITY> type of Entity
     * @param <DTO>    type of DTO
     * @return JPA specification for filtering Entities
     */
    <ENTITY, DTO> Specs<ENTITY> createSpecs(DTO dto);

    /**
     * Get dot-path for Entity's property by specified DTO class and DTO's property name
     *
     * @param dtoClass     DTO class
     * @param propertyName name of DTO's property
     * @param <DTO>        type of DTO
     * @return Entity's property path
     */
    <DTO> String getPath(Class<DTO> dtoClass, String propertyName);

    /**
     * Construct JPA path for Entity's property by specified DTO class and DTO property
     *
     * @param dtoClass     DTO class
     * @param propertyName name of DTO's property
     * @param root         JPA Root
     * @param query        JPA Criteria Query
     * @param cb           JPA Criteria Builder
     * @param <EP>         type of Entity's property
     * @param <DTO>        type of DTO
     * @return JPA Path object
     */
    <EP, DTO> Path<EP> getJpaPath(Class<DTO> dtoClass, String propertyName,
                                  Root root, CriteriaQuery query, CriteriaBuilder cb);
}

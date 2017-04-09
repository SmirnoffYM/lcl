package com.habds.lcl.core.processor.impl.ext;

import com.habds.lcl.core.processor.SetterMapping;
import com.habds.lcl.core.processor.impl.SetterPostMapping;
import com.habds.lcl.core.processor.impl.SetterPostMappingChain;
import com.habds.lcl.core.processor.impl.util.ClassCache;
import com.habds.lcl.core.processor.impl.util.Property;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.lang.reflect.Field;

/**
 * Abstract {@link SetterPostMapping} allowing to set entity's relation by searching for corresponding value
 * in the database by primary key. To active that {@link com.habds.lcl.core.annotation.Link} annotation must point to
 * this primary key of the relation
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 3/24/16 2:36 PM
 */
public abstract class JpaRelationSetterPostMapping implements SetterPostMapping {

    @Override
    public boolean isApplicable(String remainingPath, Property entityProperty,
                                Class dtoPropertyClass, Field dtoField, SetterPostMappingChain chain) {
        String[] chains = remainingPath.split("\\.");
        return chains.length >= 2 && entityProperty.getType().getAnnotation(Entity.class) != null
            && ClassCache.getInstance().hasProperty(entityProperty.getField().getType(), chains[1])
            && ClassCache.getInstance().getProperty(entityProperty.getField().getType(), chains[1])
            .hasFieldAnnotation(Id.class);
    }

    @Override
    public SetterMapping getMapping(String remainingPath, Property entityProperty,
                                    Class dtoPropertyClass, Field dtoField, SetterPostMappingChain chain) {
        return (entity, dtoProperty) -> {
            Object chainValue = dtoProperty == null
                ? null : getRelationValue(entityProperty.getField().getType(), dtoProperty);
            entityProperty.setter().apply(entity, chainValue);
            return chainValue;
        };
    }

    /**
     * Perform relation fetching
     *
     * @param entityType type of entity to fetch
     * @param primaryKey primary key value, non-null
     * @return fetched relation or null if it doesn't exist
     */
    protected abstract Object getRelationValue(Class<?> entityType, Object primaryKey);
}

package com.habds.lcl.core.data;

import com.habds.lcl.core.processor.impl.SimpleLinkProcessor;
import com.habds.lcl.core.processor.impl.SimpleProcessor;
import com.habds.lcl.core.processor.impl.util.ClassCache;
import com.habds.lcl.core.processor.impl.util.Property;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import java.lang.reflect.Field;

/**
 * Simple link processor allowing to change relations of {@link com.habds.lcl.core.annotation.ClassLink}'ed entity
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 2/26/16 8:50 PM
 */
@SuppressWarnings("unchecked")
public class JpaLinkProcessor extends SimpleLinkProcessor {

    protected EntityManager em;

    public JpaLinkProcessor(EntityManager em) {
        this.em = em;
    }

    /**
     * If intermediate property is instance of {@link Entity}-marked class (both null or not null) and
     * remaining path is it's {@link Id}, then use {@link EntityManager} to find entity by DTO's property value
     * (because it is an ID), set it into this property and stop the chain.
     * Otherwise call the parent method of {@link SimpleProcessor} class
     *
     * @param intermediateEntity        intermediate entity
     * @param intermediateProperty      intermediate entity's {@link Property}
     * @param intermediatePropertyValue current value of intermediate property
     * @param remainingPath             remaining dot-path
     * @param dtoField                  DTO's field
     * @param dtoPropertyValue          DTO field's value (to be set into Entity's property)
     * @return value was really set
     */
    @Override
    protected Object passSetterChain(Object intermediateEntity, Property intermediateProperty,
                                     Object intermediatePropertyValue, String remainingPath, Field dtoField,
                                     Object dtoPropertyValue) {
        Field sourceField = intermediateProperty.getField();
        if (!remainingPath.contains(".") && sourceField.getType().getAnnotation(Entity.class) != null) {
            Property remainingProp = ClassCache.getInstance().getProperty(sourceField.getType(), remainingPath);
            if (remainingProp != null && remainingProp.getField().getAnnotation(Id.class) != null) {
                Object chainValue = dtoPropertyValue == null
                    ? null
                    : em.find(sourceField.getType(), dtoPropertyValue);
                intermediateProperty.setter().apply(intermediateEntity, chainValue);
                return dtoPropertyValue;
            }
        }
        return super.passSetterChain(intermediateEntity, intermediateProperty, intermediatePropertyValue,
            remainingPath, dtoField, dtoPropertyValue);
    }

    public EntityManager getEm() {
        return em;
    }

    public void setEm(EntityManager em) {
        this.em = em;
    }
}

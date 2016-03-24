package com.habds.lcl.core.data;

import com.habds.lcl.core.processor.impl.SimpleLinkProcessor;
import com.habds.lcl.core.processor.impl.SimpleProcessor;
import com.habds.lcl.core.processor.impl.ext.JpaRelationSetterPostMapping;

import javax.persistence.EntityManager;

/**
 * Simple link processor allowing to change relations of {@link com.habds.lcl.core.annotation.ClassLink}'ed entity
 *
 * @author Yurii Smyrnov
 * @version 2
 * @see JpaRelationSetterPostMapping
 * @since 2/26/16 8:50 PM
 */
@SuppressWarnings("unchecked")
public class JpaLinkProcessor extends SimpleLinkProcessor {

    protected EntityManager em;

    public JpaLinkProcessor(EntityManager em) {
        this.em = em;
    }

    @Override
    public void configure(SimpleProcessor processor) {
        super.configure(processor);
        setterMappings.add(new JpaRelationSetterPostMapping() {
            @Override
            protected Object getRelationValue(Class<?> entityType, Object primaryKey) {
                return em.find(entityType, primaryKey);
            }
        });
    }

    public EntityManager getEm() {
        return em;
    }

    public void setEm(EntityManager em) {
        this.em = em;
    }
}

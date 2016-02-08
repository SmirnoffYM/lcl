package com.habds.lcl.spring;

import com.habds.lcl.core.annotation.ClassLink;
import com.habds.lcl.core.processor.LinkProcessor;
import com.habds.lcl.core.processor.impl.SimpleLinkProcessor;
import com.habds.lcl.core.processor.impl.SimpleProcessor;
import com.habds.lcl.core.processor.impl.util.ClassCache;
import com.habds.lcl.core.processor.impl.util.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.support.Repositories;

import javax.annotation.PostConstruct;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Extension of the {@link SimpleProcessor}
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 1/31/16 1:53 PM
 */
@SuppressWarnings("unchecked")
public class SpringProcessor extends SimpleProcessor {

    @Autowired
    private ApplicationContext context;

    private Repositories repositories;

    public SpringProcessor() {
        super();
        linkProcessor = new SpringLinkProcessor();
    }

    public SpringProcessor(LinkProcessor linkProcessor) {
        super(linkProcessor);
    }

    @PostConstruct
    protected void init() {
        repositories = new Repositories(context);
    }

    public SpringProcessor add(String packageName) {
        ComponentScanUtils.scan(packageName)
            .filter(c -> c.isAnnotationPresent(ClassLink.class))
            .forEach(this::add);
        return this;
    }

    public <ENTITY, DTO> JpaDao<ENTITY, DTO> dao(JpaSpecificationExecutor<ENTITY> delegate, Class<DTO> targetClass) {
        return new JpaDao<>(this, delegate, targetClass);
    }

    public <ENTITY, DTO> JpaDao<ENTITY, DTO> dao(Class<DTO> targetClass) {
        JpaSpecificationExecutor<ENTITY> delegate = (JpaSpecificationExecutor<ENTITY>) repositories
            .getRepositoryFor(targetClass.getAnnotation(ClassLink.class).value());
        return dao(delegate, targetClass);
    }

    public class SpringLinkProcessor extends SimpleLinkProcessor {

        /**
         * If intermediate property is instance of {@link Entity}-marked class (both null or not null) and
         * remaining path is it's {@link Id}, then find entity by DTO's property value
         * (because it is an ID) in corresponding JPA repository, set it into this property and stop the chain.
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
                                         Object intermediatePropertyValue,
                                         String remainingPath, Field dtoField, Object dtoPropertyValue) {
            Field sourceField = intermediateProperty.getField();
            if (!remainingPath.contains(".") && sourceField.getType().getAnnotation(Entity.class) != null) {
                Property remainingProp = ClassCache.getInstance().getProperty(sourceField.getType(), remainingPath);
                if (remainingProp != null && remainingProp.getField().getAnnotation(Id.class) != null) {
                    Object chainValue = dtoPropertyValue == null
                        ? null
                        : ((JpaRepository) repositories.getRepositoryFor(sourceField.getType()))
                        .findOne((Serializable) dtoPropertyValue);
                    intermediateProperty.setter().apply(intermediateEntity, chainValue);
                    return dtoPropertyValue;
                }
            }
            return super.passSetterChain(intermediateEntity, intermediateProperty, intermediatePropertyValue,
                remainingPath, dtoField, dtoPropertyValue);
        }
    }
}

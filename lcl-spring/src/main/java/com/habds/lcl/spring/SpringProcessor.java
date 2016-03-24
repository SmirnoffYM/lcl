package com.habds.lcl.spring;

import com.habds.lcl.core.annotation.ClassLink;
import com.habds.lcl.core.processor.LinkProcessor;
import com.habds.lcl.core.processor.impl.SimpleLinkProcessor;
import com.habds.lcl.core.processor.impl.SimpleProcessor;
import com.habds.lcl.core.processor.impl.ext.JpaRelationSetterPostMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.support.Repositories;

import javax.annotation.PostConstruct;
import java.io.Serializable;

/**
 * Extension of the {@link SimpleProcessor}
 *
 * @author Yurii Smyrnov
 * @version 2
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

        @Override
        public void configure(SimpleProcessor processor) {
            super.configure(processor);
            setterMappings.add(new JpaRelationSetterPostMapping() {
                @Override
                protected Object getRelationValue(Class<?> entityType, Object primaryKey) {
                    return ((JpaRepository) repositories.getRepositoryFor(entityType))
                        .findOne((Serializable) primaryKey);
                }
            });
        }
    }
}

package com.habds.lcl.core.processor.impl;

import com.habds.lcl.core.processor.GetterMapping;
import com.habds.lcl.core.processor.Processor;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * Chain of {@link PostMapping}s. Allows to start postmapping recursively (for collections&arrays support etc)
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 2/2/16 10:33 PM
 */
public class PostMappingChain {

    private Processor processor;
    private Set<PostMapping> postMappings;

    public PostMappingChain(Processor processor, Set<PostMapping> postMappings) {
        this.processor = processor;
        this.postMappings = postMappings;
    }

    public PostMappingChain(Processor processor, Class... postMappingClasses) {
        try {
            this.processor = processor;
            postMappings = new HashSet<>();
            for (Class postMappingClass : postMappingClasses) {
                postMappings.add((PostMapping) postMappingClass.newInstance());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public GetterMapping start(Class<?> entityPropertyClass, Class<?> dtoPropertyClass, Field dtoField) {
        for (PostMapping potMapping : postMappings) {
            if (potMapping.isApplicable(entityPropertyClass, dtoPropertyClass, dtoField, this.processor)) {
                return potMapping.getMapping(entityPropertyClass, dtoPropertyClass, dtoField, this);
            }
        }
        return (entityProperty, dto) -> entityProperty;
    }

    public Processor getProcessor() {
        return processor;
    }
}

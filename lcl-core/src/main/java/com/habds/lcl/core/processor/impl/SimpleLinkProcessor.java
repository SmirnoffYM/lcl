package com.habds.lcl.core.processor.impl;

import com.habds.lcl.core.processor.GetterMapping;
import com.habds.lcl.core.processor.LinkProcessor;
import com.habds.lcl.core.processor.Processor;
import com.habds.lcl.core.processor.SetterMapping;
import com.habds.lcl.core.processor.impl.ext.ArrayPostMapping;
import com.habds.lcl.core.processor.impl.ext.CollectionPostMapping;
import com.habds.lcl.core.processor.impl.ext.EnumPostMapping;
import com.habds.lcl.core.processor.impl.ext.RecursionPostMapping;
import com.habds.lcl.core.processor.impl.util.ClassCache;
import com.habds.lcl.core.processor.impl.util.Property;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Bindable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Default {@link LinkProcessor} implementation. Uses dot as property separator.
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 12/1/2015 1:07 AM
 */
@SuppressWarnings("unchecked")
public class SimpleLinkProcessor implements LinkProcessor<SimpleProcessor>, PostMappingChain {

    private SimpleProcessor processor;
    private List<PostMapping> mappings;

    public SimpleLinkProcessor() {
    }

    /**
     * Configure getter postmapping chain using specified processor
     *
     * @param processor processor
     */
    @Override
    public void configure(SimpleProcessor processor) {
        this.processor = processor;
        this.mappings = Arrays.asList(
            new ArrayPostMapping(), new CollectionPostMapping(), new EnumPostMapping(), new RecursionPostMapping());
    }

    @Override
    public GetterMapping getterMapping(String path, Class entityClass, Class dtoPropertyClass, Field dtoField) {
        // If there are no chains remained, perform one last post-mapping if available
        if (path.isEmpty()) {
            GetterMapping getterMapping = (s, t) -> s;
            GetterMapping appliedPostMapping
                = applyPostMapping(getterMapping, path, entityClass, dtoPropertyClass, dtoField);
            return appliedPostMapping != null ? appliedPostMapping : getterMapping;
        }

        String[] splittedPath = path.split("\\.", 2);
        String propertyName = splittedPath[0];
        String remainingPath = splittedPath.length == 2 ? splittedPath[1] : "";

        Class entityPropertyClass;
        Function getter;
        if (ClassCache.getInstance().hasProperty(entityClass, propertyName)) {
            Property property = ClassCache.getInstance().getProperty(entityClass, propertyName);
            entityPropertyClass = property.getType();
            getter = property.getter();
        } else if (ClassCache.getInstance().hasGetterMethod(entityClass, propertyName)) {
            Method getterMethod = ClassCache.getInstance().getGetterMethod(entityClass, propertyName);
            entityPropertyClass = getterMethod.getReturnType();
            getter = (entity) -> ClassCache.invoke(entity, getterMethod);
        } else {
            throw new IllegalStateException("Cannot access " + propertyName + " from " + entityClass
                + ", no such field or corresponding public getter method");
        }

        // Extract value
        GetterMapping getterMapping = (s, t) -> s == null ? null : getter.apply(s);

        // Perform post-extracting mapping if available
        GetterMapping appliedPostMapping
            = applyPostMapping(getterMapping, remainingPath, entityPropertyClass, dtoPropertyClass, dtoField);
        if (appliedPostMapping != null) {
            return appliedPostMapping;
        }

        // If there are more chains in the path, continue mapping
        if (!remainingPath.isEmpty()) {
            return getterMapping.andThen(
                getterMapping(remainingPath, entityPropertyClass, dtoPropertyClass, dtoField));
        }

        // Otherwise return extracted value
        return getterMapping;
    }

    private GetterMapping applyPostMapping(GetterMapping getterMapping, String remainingPath, Class entityPropertyClass,
                                           Class dtoPropertyClass, Field dtoField) {
        for (PostMapping mapping : mappings) {
            if (mapping.isApplicable(remainingPath, entityPropertyClass, dtoPropertyClass, dtoField, this)) {
                return getterMapping.andThen(
                    mapping.getMapping(remainingPath, entityPropertyClass, dtoPropertyClass, dtoField, this));
            }
        }
        return null;
    }

    @Override
    public GetterMapping getterMapping(String path, Class entityClass, Field dtoField) {
        return getterMapping(path, entityClass, dtoField.getType(), dtoField);
    }

    @Override
    public SetterMapping setterMapping(String path, Class entityClass, Field dtoField) {
        String propertyName = path.split("\\.", 2)[0];
        if (!ClassCache.getInstance().hasProperty(entityClass, propertyName) &&
            ClassCache.getInstance().hasGetterMethod(entityClass, propertyName)) {
            return (s, v) -> v;
        }
        Property property = ClassCache.getInstance().getProperty(entityClass, propertyName);

        //TODO: mapping
        if (path.contains(".")) {
            String remainingPath = path.substring(path.indexOf(".") + 1);
            return (s, v) -> passSetterChain(s, property, property.getter().apply(s), remainingPath, dtoField, v);
        } else {
            return (s, v) -> property.setter().apply(s, v);
        }
    }

    @Override
    public Processor getProcessor() {
        return processor;
    }

    /**
     * Method determines what should be done with intermediate property
     * (located not in the end of property dot-path)
     * during setting DTO's property value to Entity's property.
     * This implementation only replaces {@code null} with new class instance (to avoid NPE during setting). If
     * property already has a value, it just leaves it and goes further.
     *
     * @param intermediateEntity        intermediate entity
     * @param intermediateProperty      intermediate entity's {@link Property}
     * @param intermediatePropertyValue current value of intermediate property
     * @param remainingPath             remaining dot-path
     * @param dtoField                  DTO's field
     * @param dtoPropertyValue          DTO field's value (to be set into Entity's property)
     * @return value was really set
     */
    protected Object passSetterChain(Object intermediateEntity, Property intermediateProperty,
                                     Object intermediatePropertyValue,
                                     String remainingPath, Field dtoField, Object dtoPropertyValue) {
        if (intermediatePropertyValue == null) {
            intermediatePropertyValue = ClassCache.construct(intermediateProperty.getField().getType());
            intermediateProperty.setter().apply(intermediateEntity, intermediatePropertyValue);
        }
        return setterMapping(remainingPath, intermediateProperty.getType(), dtoField)
            .map(intermediatePropertyValue, dtoPropertyValue);
    }

    @Override
    public String getPath(Class dtoClass, String propertyName) {
        String[] divided = propertyName.split("\\.", 2);
        Field field = ClassCache.getInstance().getProperty(dtoClass, divided[0]).getField();
        String entityPath = MappingMetadata.toDotPath(field);
        if (divided[0].equals(propertyName)) {
            return entityPath;
        }
        return entityPath + "." + getPath(field.getType(), divided[1]);
    }

    @Override
    public Path getJpaPath(Class dtoClass, String propertyName, Root root, CriteriaQuery query, CriteriaBuilder cb) {
        String entityPath = getPath(dtoClass, propertyName);
        return getJpaPath(root, entityPath);
    }

    private static Path getJpaPath(From from, String property) {
        String[] chains = property.split("\\.", 2);
        if (doJoin(from, chains[0])) {
            Join join = join(from, chains[0]);
            return chains.length == 1 ? join : getJpaPath(join, chains[1]);
        }
        Path path = from.get(chains[0]);
        return chains.length == 1 ? path : getJpaPath(path, chains[1]);
    }

    private static Path getJpaPath(Path path, String property) {
        String[] chains = property.split("\\.", 2);
        path = path.get(chains[0]);
        return chains.length == 1 ? path : getJpaPath(path, chains[1]);
    }

    private static boolean doJoin(From from, String chain) {
        Bindable model = from.get(chain).getModel();
        if (!(model instanceof Attribute)) {
            return false;
        }
        Attribute.PersistentAttributeType type = ((Attribute<?, ?>) model).getPersistentAttributeType();
        return type != Attribute.PersistentAttributeType.BASIC && type != Attribute.PersistentAttributeType.EMBEDDED;
    }

    private static Join join(From<?, ?> from, String chain) {
        for (Join<?, ?> join : from.getJoins()) {
            if (join.getAttribute().getName().equals(chain) && join.getJoinType().equals(JoinType.LEFT)) {
                return join;
            }
        }
        return from.join(chain, JoinType.LEFT);
    }
}

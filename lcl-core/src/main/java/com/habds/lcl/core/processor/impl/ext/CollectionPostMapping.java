package com.habds.lcl.core.processor.impl.ext;

import com.habds.lcl.core.annotation.Contains;
import com.habds.lcl.core.processor.GetterMapping;
import com.habds.lcl.core.processor.impl.PostMapping;
import com.habds.lcl.core.processor.impl.PostMappingChain;
import com.habds.lcl.core.processor.impl.util.ClassCache;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;

/**
 * {@link PostMapping} for collection fields. Handles elements customization specified by {@link Contains} annotation.
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 2/2/16 10:52 PM
 */
@SuppressWarnings("unchecked")
public class CollectionPostMapping implements PostMapping {

    @Override
    public boolean isApplicable(String remainingPath, Class entityPropertyClass, Class dtoPropertyClass, Field dtoField,
                                PostMappingChain chain) {
        return Collection.class.isAssignableFrom(entityPropertyClass)
            && dtoField != null && Collection.class.isAssignableFrom(dtoPropertyClass);
    }

    @Override
    public GetterMapping getMapping(String remainingPath, Class entityPropertyClass,
                                    Class dtoPropertyClass, Field dtoField,
                                    PostMappingChain chain) {
        Class<?> dtoCollectionType = getDtoEmptyCollectionType(dtoField.getType());
        Contains annotation = dtoField.getAnnotation(Contains.class);
        Class<?> dtoElementType = annotation == null ? Object.class : annotation.value();
        return (entityProperty, dto) -> {
                Collection collection = (Collection) ClassCache.getInstance()
                    .getProperty((Class) dto.getClass(), dtoField.getName()).getter().apply(dto);
                if (collection == null) {
                    collection = (Collection) ClassCache.construct(dtoCollectionType);
                }
                for (Object element : (Collection) entityProperty) {
                    collection.add(
                        chain.getterMapping(remainingPath, element.getClass(), dtoElementType, null).map(element, dto));
                }
                return collection;
        };
    }

    public Class<?> getDtoEmptyCollectionType(Class<?> dtoClass) {
        // If for non-abstract collection class there is public constructor with no arguments,
        // return the original DTO type
        if (!Modifier.isAbstract(dtoClass.getModifiers())
            && Stream.of(dtoClass.getConstructors())
            .filter(c -> c.getParameterCount() == 0 && Modifier.isPublic(c.getModifiers())).count() == 1) {
            return dtoClass;
        } else if (List.class.isAssignableFrom(dtoClass)) {
            return ArrayList.class;
        } else if (Set.class.isAssignableFrom(dtoClass)) {
            return HashSet.class;
        } else if (Queue.class.isAssignableFrom(dtoClass)) {
            return LinkedList.class;
        } else {
            throw new IllegalArgumentException("Collection type " + dtoClass + " is not supported");
        }
    }
}

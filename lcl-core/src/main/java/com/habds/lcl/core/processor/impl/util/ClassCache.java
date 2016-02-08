package com.habds.lcl.core.processor.impl.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Cached information about class fields and properties
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 12/1/2015 1:38 AM
 */
@SuppressWarnings("unchecked")
public class ClassCache {

    private static final ClassCache INSTANCE = new ClassCache();

    // Cached fields for all classes visited this object
    private Map<Class, Map<String, Field>> classFields = new HashMap<>();
    // Cached properties
    private Map<Class, Map<String, Property>> classProperties = new HashMap<>();

    private ClassCache() {
    }

    public static ClassCache getInstance() {
        return INSTANCE;
    }

    public static <T> T construct(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <S, P> Property<S, P> getProperty(Class<S> clazz, String propertyName) {
        return classProperties
            // initialize map for each class if it is absent
            .computeIfAbsent(clazz, c -> new HashMap<>())
            // create a Property object if it is absent
            .computeIfAbsent(propertyName, p -> new Property<>(clazz, getAllFields(clazz).get(p)));
    }

    public <S> boolean hasProperty(Class<S> clazz, String propertyName) {
        Map<String, Property> properties = classProperties.get(clazz);
        return properties != null && properties.containsKey(propertyName);
    }

    public void setPropertyValue(Object bean, String name, Object value) {
        try {
            classFields.get(bean.getClass()).get(name).set(bean, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Field> getAllFields(Class clazz) {
        if (classFields.containsKey(clazz)) {
            return classFields.get(clazz);
        }
        cacheFields(clazz);
        return classFields.get(clazz);
    }

    public void cacheFields(Class clazz) {
        Map<String, Field> fields = Arrays.asList(clazz.getDeclaredFields()).stream()
            .peek(f -> f.setAccessible(true))
            .collect(Collectors.toMap(Field::getName, Function.identity()));
        if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
            fields.putAll(getAllFields(clazz.getSuperclass()));
        }
        classFields.put(clazz, fields);
    }
}

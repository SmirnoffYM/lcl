package com.habds.lcl.core.processor.impl.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BinaryOperator;
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
    // Cached getters (getXXX() or isXXX() for boolean return type) for all classes visited this object
    private Map<Class, Map<String, Method>> classGetters = new HashMap<>();
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

    public static Object invoke(Object o, Method method, Object... args) {
        try {
            return method.invoke(o, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <S, P> Property<S, P> getProperty(Class<S> clazz, String propertyName) {
        return classProperties
            // initialize map for each class if it is absent
            .computeIfAbsent(clazz, c -> new HashMap<>())
            // create a Property object if it is absent
            .computeIfAbsent(propertyName, p -> new Property<>(clazz, p));
    }

    public <S> boolean hasProperty(Class<S> clazz, String propertyName) {
        // Optimize this method
        try {
            return getProperty(clazz, propertyName) != null;
        } catch (RuntimeException e) {
            return false;
        }
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
        cacheFieldsAndMethods(clazz);
        return classFields.get(clazz);
    }

    public Map<String, Method> getAllGetters(Class clazz) {
        if (classGetters.containsKey(clazz)) {
            return classGetters.get(clazz);
        }
        cacheFieldsAndMethods(clazz);
        return classGetters.get(clazz);
    }

    public boolean hasGetterMethod(Class clazz, String propertyName) {
        return classGetters.get(clazz).containsKey(propertyName);
    }

    public Method getGetterMethod(Class clazz, String propertyName) {
        return classGetters.get(clazz).get(propertyName);
    }

    public Map<String, Property> getAllProperties(Class clazz) {
        return getAllFields(clazz).entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> new Property(clazz, e.getKey())));
    }

    public void cacheFieldsAndMethods(Class clazz) {
        Map<String, Field> fields = Arrays.asList(clazz.getDeclaredFields()).stream()
            .peek(f -> f.setAccessible(true))
            .collect(Collectors.toMap(Field::getName, Function.identity()));
        if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
            fields.putAll(getAllFields(clazz.getSuperclass()));
        }
        classFields.put(clazz, fields);

        Map<String, Method> methods = Arrays.asList(clazz.getMethods()).stream()
            .filter(this::isGetter)
            .collect(Collectors.toMap(this::getPropertyNameFromGetter, Function.identity(), new MethodMerger()));
        classGetters.put(clazz, methods);
    }

    private boolean isGetter(Method method) {
        String name = method.getName();
        return method.getParameterCount() == 0 && method.getReturnType() != void.class
            && method.getReturnType() != Void.class
            && ((name.startsWith("get") && Objects.equals(name.substring(3, 4).toUpperCase(), name.substring(3, 4)))
            || (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)
            && name.startsWith("is") && Objects.equals(name.substring(2, 3).toUpperCase(), name.substring(2, 3)));
    }

    private String getPropertyNameFromGetter(Method getter) {
        String name = getter.getName().replaceFirst("get|is", "");
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    public static class MethodMerger implements BinaryOperator<Method> {

        @Override
        public Method apply(Method m1, Method m2) {
            return m1.getClass().isAssignableFrom(m2.getClass()) ? m2 : m1;
        }
    }
}

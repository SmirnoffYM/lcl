package com.habds.lcl.spring;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.util.ClassUtils.convertClassNameToResourcePath;
import static org.springframework.util.SystemPropertyUtils.resolvePlaceholders;

/**
 * Utility class for component-scan
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 1/31/16 1:47 PM
 */
public class ComponentScanUtils {

    public static List<Class> scanAll(String packageName) {
        return scan(packageName).collect(Collectors.toList());
    }

    public static Stream<Class> scan(String packageName) {
        try {
            ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
            MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                + convertClassNameToResourcePath(resolvePlaceholders(packageName))
                + "/**/*.class";

            return Stream.of(resourcePatternResolver.getResources(packageSearchPath))
                .filter(Resource::isReadable)
                .map(r -> getClassOrNull(readerFactory, r))
                .filter(c -> c != null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Class getClassOrNull(MetadataReaderFactory readerFactory, Resource resource) {
        try {
            MetadataReader metadataReader = readerFactory.getMetadataReader(resource);
            return Class.forName(metadataReader.getClassMetadata().getClassName());
        } catch (Exception e) {
            return null;
        }
    }
}

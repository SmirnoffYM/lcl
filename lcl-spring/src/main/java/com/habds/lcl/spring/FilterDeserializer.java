package com.habds.lcl.spring;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.habds.lcl.core.data.filter.Filter;
import com.habds.lcl.core.data.filter.impl.Equals;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Deserializer decides what filter should be chosen by specified {@link FilterDeserializer#TYPE_PROPERTY} value
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 1/31/16 12:57 AM
 */
public class FilterDeserializer extends JsonDeserializer<Filter> {

    public static final String DEFAULT_FILTERS_PACKAGE = "com.habds.lcl.core.data.filter.impl";
    public static final String TYPE_PROPERTY = "$type";

    private List<Class<? extends Filter>> filterClasses = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void setUp() throws Exception {
        filterClasses.addAll(ComponentScanUtils.scan(DEFAULT_FILTERS_PACKAGE)
            .filter(clazz -> Filter.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers()))
            .map(clazz -> (Class<Filter>) clazz)
            .collect(Collectors.toList()));
    }

    @Override
    public Filter deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        ObjectMapper oc = (ObjectMapper) parser.getCodec();
        ObjectNode node = oc.readTree(parser);
        JsonNode typeNode = node.get(TYPE_PROPERTY);
        Class<? extends Filter> filterClass;
        if (typeNode != null) {
            String type = typeNode.asText();
            filterClass = filterClasses.stream()
                .filter(c -> c.getSimpleName().toLowerCase().equalsIgnoreCase(type))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Invalid filter type: " + type));
        } else {
            filterClass = Equals.class;
        }

        return oc.treeToValue(node, filterClass);
    }
}

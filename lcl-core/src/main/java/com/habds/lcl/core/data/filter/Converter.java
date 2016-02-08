package com.habds.lcl.core.data.filter;

import javax.persistence.criteria.Path;

/**
 * Class performs incoming filter value conversions to provide support of different types of fields
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 2/1/16 1:00 AM
 */
public abstract class Converter {

    protected Path entityPropertyPath;

    public Converter(Path entityPropertyPath) {
        this.entityPropertyPath = entityPropertyPath;
    }

    /**
     * Convert value
     *
     * @param filterValue non-nullable filter value
     * @return conversion result
     */
    public abstract Object convert(Object filterValue);
}

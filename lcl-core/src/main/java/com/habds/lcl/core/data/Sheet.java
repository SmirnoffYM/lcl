package com.habds.lcl.core.data;

import java.util.List;
import java.util.stream.Stream;

/**
 * Paged result
 *
 * @author Yurii Smyrnov
 * @version 1
 * @see PagingAndSorting
 * @since 2/16/16 8:04 PM
 */
public class Sheet<E> extends PagingAndSorting {

    private Long totalPages;
    private long totalElements;
    private List<E> content;

    public Sheet(List<E> content, long totalElements, PagingAndSorting pageSettings) {
        super(pageSettings.getPage(), pageSettings.getPageSize(), pageSettings.getSortings());
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = pageSize == null ? null : (long) Math.ceil((double) totalElements / (double) pageSize);
    }

    public Stream<E> stream() {
        return content.stream();
    }

    public long size() {
        return content.size();
    }

    public long getTotalPages() {
        return totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public List<E> getContent() {
        return content;
    }
}

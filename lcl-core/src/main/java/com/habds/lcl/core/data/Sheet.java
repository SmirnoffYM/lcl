package com.habds.lcl.core.data;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Paged result
 *
 * @author Yurii Smyrnov
 * @version 2
 * @see PagingAndSorting
 * @since 2/16/16 8:04 PM
 */
public class Sheet<E> {

    private Long totalPages;
    private long totalElements;
    private List<E> content;
    private PagingAndSorting pageable;

    public Sheet(List<E> content, long totalElements, PagingAndSorting pageable) {
        this.pageable = pageable;
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = getPageSize() == null
            ? null : (long) Math.ceil((double) totalElements / (double) getPageSize());
    }

    public Integer getPage() {
        return pageable.getPage();
    }

    public Integer getPageSize() {
        return pageable.getPageSize();
    }

    public Map<String, Boolean> getSortings() {
        return pageable.getSortings();
    }

    public Stream<E> stream() {
        return content.stream();
    }

    @SuppressWarnings("unchecked")
    public <T> Sheet<T> map(Function<E, T> mapper) {
        content = (List) stream().map(mapper::apply).collect(Collectors.toList());
        return (Sheet) this;
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

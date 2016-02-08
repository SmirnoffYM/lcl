package com.habds.lcl.core.data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class holds data about query pagination and sorting
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 1/4/16 11:25 PM
 */
public class PagingAndSorting {

    private Integer page;
    private Integer pageSize;
    private Map<String, Boolean> sortings = new LinkedHashMap<>();

    public PagingAndSorting() {
    }

    public PagingAndSorting(Integer page, Integer pageSize) {
        withPagination(page, pageSize);
    }

    public PagingAndSorting(String property) {
        orderBy(property);
    }

    public PagingAndSorting(String property, boolean ascending) {
        orderBy(property, ascending);
    }

    public PagingAndSorting withPagination(Integer page, Integer pageSize) {
        this.page = page;
        this.pageSize = pageSize;
        return this;
    }

    public PagingAndSorting orderBy(String property, boolean ascending) {
        sortings.put(property, ascending);
        return this;
    }

    public PagingAndSorting orderBy(String property) {
        return orderBy(property, true);
    }

    public Integer getPage() {
        return page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public Map<String, Boolean> getSortings() {
        return sortings;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public void setSortings(Map<String, Boolean> sortings) {
        this.sortings = sortings;
    }
}

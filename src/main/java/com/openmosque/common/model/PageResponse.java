package com.openmosque.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Standard Paginated Response Model.
 * 
 * WHY THIS IS PRESENT:
 * Adapts Spring Data's 'Page<T>' into a clean, lightweight JSON format
 * providing all necessary pagination metadata (total pages, current page, has next)
 * for infinite-scroll on React Native mobile app and pagination controls on React Web.
 * 
 * @param <T> The item type inside the paginated list
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    /**
     * The list of items for the current page slice.
     */
    private List<T> content;

    /**
     * Zero-indexed page number.
     */
    private int pageNumber;

    /**
     * Number of items per page requested.
     */
    private int pageSize;

    /**
     * Total number of elements matching the query across all pages.
     */
    private long totalElements;

    /**
     * Total number of pages available.
     */
    private int totalPages;

    /**
     * 'true' if this is the very first page.
     */
    private boolean isFirst;

    /**
     * 'true' if this is the last page.
     */
    private boolean isLast;

    /**
     * 'true' if subsequent pages exist.
     */
    private boolean hasNext;

    /**
     * 'true' if previous pages exist.
     */
    private boolean hasPrevious;

    /**
     * Converts a Spring Data Page directly into a PageResponse wrapper.
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    /**
     * Converts a Spring Data Page while applying a mapped list of DTOs.
     */
    public static <T, R> PageResponse<R> from(Page<T> page, List<R> mappedContent) {
        return PageResponse.<R>builder()
                .content(mappedContent)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}

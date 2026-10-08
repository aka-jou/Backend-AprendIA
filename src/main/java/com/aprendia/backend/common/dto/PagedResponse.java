package com.aprendia.backend.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> content;
    private PageableInfo pageable;
    private long totalElements;
    private int totalPages;
    private boolean last;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PageableInfo {
        private int pageNumber;
        private int pageSize;
    }

    public static <T> PagedResponse<T> fromPage(Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .pageable(new PageableInfo(page.getNumber(), page.getSize()))
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}

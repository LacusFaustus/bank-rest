package com.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> content;
    private PaginationMetadata pagination;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationMetadata {
        private int currentPage;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
    }

    public static <T> PaginatedResponse<T> of(Page<T> page) {
        PaginationMetadata metadata = new PaginationMetadata(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
        return new PaginatedResponse<>(page.getContent(), metadata);
    }
}

package com.bank.dto;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaginatedResponseTest {

    @Test
    void shouldCreatePaginatedResponse() {
        List<String> content = Arrays.asList("item1", "item2", "item3");
        PaginatedResponse.PaginationMetadata metadata = new PaginatedResponse.PaginationMetadata(
                0, 10, 30, 3, true, false);

        PaginatedResponse<String> response = new PaginatedResponse<>(content, metadata);

        assertNotNull(response);
        assertEquals(content, response.getContent());
        assertEquals(metadata, response.getPagination());
    }

    @Test
    void shouldCreatePaginationMetadata() {
        int currentPage = 1;
        int pageSize = 20;
        long totalElements = 100;
        int totalPages = 5;
        boolean first = false;
        boolean last = false;

        PaginatedResponse.PaginationMetadata metadata = new PaginatedResponse.PaginationMetadata(
                currentPage, pageSize, totalElements, totalPages, first, last);

        assertEquals(currentPage, metadata.getCurrentPage());
        assertEquals(pageSize, metadata.getPageSize());
        assertEquals(totalElements, metadata.getTotalElements());
        assertEquals(totalPages, metadata.getTotalPages());
        assertEquals(first, metadata.isFirst());
        assertEquals(last, metadata.isLast());
    }

    @Test
    void shouldCreateFromPage() {
        List<String> content = Arrays.asList("item1", "item2", "item3");
        PageRequest pageable = PageRequest.of(0, 10);
        Page<String> page = new PageImpl<>(content, pageable, 30);

        PaginatedResponse<String> response = PaginatedResponse.of(page);

        assertNotNull(response);
        assertEquals(content, response.getContent());

        PaginatedResponse.PaginationMetadata metadata = response.getPagination();
        assertNotNull(metadata);
        assertEquals(0, metadata.getCurrentPage());
        assertEquals(10, metadata.getPageSize());
        assertEquals(30, metadata.getTotalElements());
        assertEquals(3, metadata.getTotalPages());
        assertTrue(metadata.isFirst());
        assertFalse(metadata.isLast());
    }

    @Test
    void testNoArgsConstructor() {
        PaginatedResponse<String> response = new PaginatedResponse<>();

        assertNotNull(response);
        assertNull(response.getContent());
        assertNull(response.getPagination());
    }

    @Test
    void testAllArgsConstructor() {
        List<String> content = Arrays.asList("item1", "item2", "item3");
        PaginatedResponse.PaginationMetadata metadata = new PaginatedResponse.PaginationMetadata(
                0, 10, 30, 3, true, false);

        PaginatedResponse<String> response = new PaginatedResponse<>(content, metadata);

        assertEquals(content, response.getContent());
        assertEquals(metadata, response.getPagination());
    }

    @Test
    void testPaginationMetadataGettersAndSetters() {
        PaginatedResponse.PaginationMetadata metadata = new PaginatedResponse.PaginationMetadata();

        metadata.setCurrentPage(2);
        metadata.setPageSize(20);
        metadata.setTotalElements(100L);
        metadata.setTotalPages(5);
        metadata.setFirst(false);
        metadata.setLast(false);

        assertEquals(2, metadata.getCurrentPage());
        assertEquals(20, metadata.getPageSize());
        assertEquals(100L, metadata.getTotalElements());
        assertEquals(5, metadata.getTotalPages());
        assertFalse(metadata.isFirst());
        assertFalse(metadata.isLast());
    }

    @Test
    void testPaginationMetadataNoArgsConstructor() {
        PaginatedResponse.PaginationMetadata metadata = new PaginatedResponse.PaginationMetadata();

        assertNotNull(metadata);
        assertEquals(0, metadata.getCurrentPage());
        assertEquals(0, metadata.getPageSize());
        assertEquals(0, metadata.getTotalElements());
        assertEquals(0, metadata.getTotalPages());
        assertFalse(metadata.isFirst());
        assertFalse(metadata.isLast());
    }

    @Test
    void testPaginationMetadataAllArgsConstructor() {
        PaginatedResponse.PaginationMetadata metadata = new PaginatedResponse.PaginationMetadata(
                1, 20, 100L, 5, false, false);

        assertEquals(1, metadata.getCurrentPage());
        assertEquals(20, metadata.getPageSize());
        assertEquals(100L, metadata.getTotalElements());
        assertEquals(5, metadata.getTotalPages());
        assertFalse(metadata.isFirst());
        assertFalse(metadata.isLast());
    }

    @Test
    void testGetDataMethod() {
        PaginatedResponse<String> response = new PaginatedResponse<>();

        assertNull(response.getData());
    }

    @Test
    void testEqualsAndHashCode() {
        List<String> content1 = Arrays.asList("item1", "item2");
        PaginatedResponse.PaginationMetadata metadata1 = new PaginatedResponse.PaginationMetadata(
                0, 10, 20L, 2, true, false);

        List<String> content2 = Arrays.asList("item1", "item2");
        PaginatedResponse.PaginationMetadata metadata2 = new PaginatedResponse.PaginationMetadata(
                0, 10, 20L, 2, true, false);

        List<String> content3 = Arrays.asList("item3", "item4");
        PaginatedResponse.PaginationMetadata metadata3 = new PaginatedResponse.PaginationMetadata(
                1, 10, 20L, 2, false, true);

        PaginatedResponse<String> response1 = new PaginatedResponse<>(content1, metadata1);
        PaginatedResponse<String> response2 = new PaginatedResponse<>(content2, metadata2);
        PaginatedResponse<String> response3 = new PaginatedResponse<>(content3, metadata3);

        assertEquals(response1, response2);
        assertNotEquals(response1, response3);
        assertEquals(response1.hashCode(), response2.hashCode());
        assertNotEquals(response1.hashCode(), response3.hashCode());
    }

    @Test
    void testToString() {
        List<String> content = Arrays.asList("item1");
        PaginatedResponse.PaginationMetadata metadata = new PaginatedResponse.PaginationMetadata(
                0, 10, 1L, 1, true, true);

        PaginatedResponse<String> response = new PaginatedResponse<>(content, metadata);

        String toString = response.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("PaginatedResponse"));
    }

    @Test
    void testOfMethodWithEmptyPage() {
        List<String> emptyList = Collections.emptyList();
        PageRequest pageable = PageRequest.of(0, 10);
        Page<String> page = new PageImpl<>(emptyList, pageable, 0);

        PaginatedResponse<String> response = PaginatedResponse.of(page);

        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());

        PaginatedResponse.PaginationMetadata metadata = response.getPagination();
        assertEquals(0, metadata.getCurrentPage());
        assertEquals(10, metadata.getPageSize());
        assertEquals(0, metadata.getTotalElements());
        assertEquals(0, metadata.getTotalPages());
        assertTrue(metadata.isFirst());
        assertTrue(metadata.isLast());
    }

    @Test
    void testOfMethodWithDifferentTypes() {
        List<Integer> intContent = Arrays.asList(1, 2, 3);
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Integer> intPage = new PageImpl<>(intContent, pageable, 30);

        PaginatedResponse<Integer> intResponse = PaginatedResponse.of(intPage);
        assertEquals(intContent, intResponse.getContent());

        List<Double> doubleContent = Arrays.asList(1.1, 2.2, 3.3);
        Page<Double> doublePage = new PageImpl<>(doubleContent, pageable, 30);

        PaginatedResponse<Double> doubleResponse = PaginatedResponse.of(doublePage);
        assertEquals(doubleContent, doubleResponse.getContent());
    }

    @Test
    void testPaginationMetadataToString() {
        PaginatedResponse.PaginationMetadata metadata = new PaginatedResponse.PaginationMetadata(
                1, 20, 100L, 5, false, true);

        String toString = metadata.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("PaginationMetadata"));
    }

    @Test
    void testPaginationMetadataEqualsAndHashCode() {
        PaginatedResponse.PaginationMetadata metadata1 = new PaginatedResponse.PaginationMetadata(
                1, 20, 100L, 5, false, true);

        PaginatedResponse.PaginationMetadata metadata2 = new PaginatedResponse.PaginationMetadata(
                1, 20, 100L, 5, false, true);

        PaginatedResponse.PaginationMetadata metadata3 = new PaginatedResponse.PaginationMetadata(
                2, 30, 200L, 7, true, false);

        assertEquals(metadata1, metadata2);
        assertNotEquals(metadata1, metadata3);
        assertEquals(metadata1.hashCode(), metadata2.hashCode());
        assertNotEquals(metadata1.hashCode(), metadata3.hashCode());
    }

    @Test
    void testSetContentAndPagination() {
        PaginatedResponse<String> response = new PaginatedResponse<>();

        List<String> content = Arrays.asList("item1", "item2");
        PaginatedResponse.PaginationMetadata metadata = new PaginatedResponse.PaginationMetadata(
                0, 10, 20L, 2, true, false);

        response.setContent(content);
        response.setPagination(metadata);

        assertEquals(content, response.getContent());
        assertEquals(metadata, response.getPagination());
    }

    @Test
    void testOfMethod_NullPage_ShouldThrowException() {
        assertThrows(NullPointerException.class, () -> PaginatedResponse.of(null));
    }

    @Test
    void testPaginationMetadata_WithDefaultValues() {
        PaginatedResponse.PaginationMetadata metadata = new PaginatedResponse.PaginationMetadata();

        assertEquals(0, metadata.getCurrentPage());
        assertEquals(0, metadata.getPageSize());
        assertEquals(0L, metadata.getTotalElements());
        assertEquals(0, metadata.getTotalPages());
        assertFalse(metadata.isFirst());
        assertFalse(metadata.isLast());
    }

    @Test
    void testSetAndGetMethods() {
        PaginatedResponse<String> response = new PaginatedResponse<>();
        List<String> content = List.of("test");
        PaginatedResponse.PaginationMetadata metadata = new PaginatedResponse.PaginationMetadata();

        response.setContent(content);
        response.setPagination(metadata);

        assertEquals(content, response.getContent());
        assertEquals(metadata, response.getPagination());
    }
}

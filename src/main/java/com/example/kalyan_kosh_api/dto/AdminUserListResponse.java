package com.example.kalyan_kosh_api.dto;

import lombok.Data;

import java.util.List;

/**
 * Generic paginated response wrapper
 */
@Data
public class AdminUserListResponse {
    private List<AdminUserResponse> users;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int size;
    private boolean hasNext;
    private boolean hasPrevious;
    
    public AdminUserListResponse(List<AdminUserResponse> users, int currentPage, int totalPages, 
                               long totalElements, int size, boolean hasNext, boolean hasPrevious) {
        this.users = users;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.size = size;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }
}
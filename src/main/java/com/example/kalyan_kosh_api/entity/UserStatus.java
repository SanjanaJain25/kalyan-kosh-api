package com.example.kalyan_kosh_api.entity;

/**
 * User Status for Admin Management:
 * - ACTIVE: User can login and use the system normally
 * - BLOCKED: User is blocked and cannot login
 * - DELETED: User is marked as deleted (soft delete)
 */
public enum UserStatus {
    ACTIVE,
    BLOCKED,
    DELETED
}
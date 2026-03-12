package com.example.kalyan_kosh_api.entity;

/**
 * Query Status for Manager Query System workflow
 */
public enum QueryStatus {
    PENDING,      // Newly created, waiting for assignment
    IN_PROGRESS,  // Being worked on
    RESOLVED,     // Successfully resolved
    REJECTED,     // Rejected with reason
    ESCALATED     // Escalated to higher level
}
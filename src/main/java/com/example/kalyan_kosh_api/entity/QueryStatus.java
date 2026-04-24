package com.example.kalyan_kosh_api.entity;

/**
 * Query Status for Manager Query System workflow
 */
public enum QueryStatus {
    PENDING,              // लंबित
    RESOLVED,             // समाधानित
    CANCEL,               // निरस्त
    NEED_CLARIFICATION    // स्पष्टीकरण आवश्यक
}
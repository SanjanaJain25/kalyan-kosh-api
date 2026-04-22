package com.example.kalyan_kosh_api.entity;

public enum AuditActionType {
    CREATE,
    EDIT,
    SOFT_DELETE,
    RESTORE,
    DELETE_REQUEST_CREATED,
    DELETE_REQUEST_APPROVED,
    DELETE_REQUEST_REJECTED,
    HARD_DELETE,
    CLEAR_TRASH_REQUESTED,
    CLEAR_TRASH_APPROVED
}
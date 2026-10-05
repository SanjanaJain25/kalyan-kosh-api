package com.example.kalyan_kosh_api.entity;

/**
 * Member lifecycle/tracking status.
 * Kept separate from UserStatus so account access/delete state is not mixed
 * with the member's lifecycle classification.
 */
public enum MemberStatus {
    NORMAL,
    RETIRED,
    DECEASED
}

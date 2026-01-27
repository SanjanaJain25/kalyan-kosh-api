package com.example.kalyan_kosh_api.entity;

/**
 * User Roles:
 * - ROLE_USER: Regular member (can make sahyog payments)
 * - ROLE_SAMBHAG_MANAGER: Sambhag Level Manager (view/download Sambhag data, add comments)
 * - ROLE_DISTRICT_MANAGER: District Level Manager (view/download District data, add comments)
 * - ROLE_BLOCK_MANAGER: Block Level Manager (view/download Block data, add comments)
 * - ROLE_ADMIN: Super Admin (full control)
 */
public enum Role {
    ROLE_USER,
    ROLE_SAMBHAG_MANAGER,    // Sambhag Level (संभाग प्रबंधक)
    ROLE_DISTRICT_MANAGER,   // District Level (जिला प्रबंधक)
    ROLE_BLOCK_MANAGER,      // Block Level (ब्लॉक प्रबंधक)
    ROLE_ADMIN
}

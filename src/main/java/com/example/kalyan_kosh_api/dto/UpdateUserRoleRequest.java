package com.example.kalyan_kosh_api.dto;

import com.example.kalyan_kosh_api.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for updating user role by admin
 */
@Data
public class UpdateUserRoleRequest {
    @NotNull(message = "Role is required")
    private Role role;
}
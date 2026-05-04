package com.example.kalyan_kosh_api.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class ExportMobilePermissionResponse {

    private Long id;

    private String userId;
    private String userName;
    private String userRole;
    private String userStatus;
    private String mobileNumber;

    private boolean enabled;

    private String grantedById;
    private String grantedByName;
    private Instant grantedAt;

    private String revokedById;
    private String revokedByName;
    private Instant revokedAt;

    private String remarks;
}
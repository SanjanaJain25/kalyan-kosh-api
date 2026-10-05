package com.example.kalyan_kosh_api.dto;

import com.example.kalyan_kosh_api.entity.MemberStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMemberStatusRequest {
    @NotNull(message = "Member status is required")
    private MemberStatus memberStatus;
}

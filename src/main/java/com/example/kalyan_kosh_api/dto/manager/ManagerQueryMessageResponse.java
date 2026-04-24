package com.example.kalyan_kosh_api.dto.manager;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerQueryMessageResponse {

    private Long id;

    private String senderId;
    private String senderName;
    private String senderRole;

    private String message;

    private Instant createdAt;
}
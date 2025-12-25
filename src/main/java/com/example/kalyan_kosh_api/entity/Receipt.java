package com.example.kalyan_kosh_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "death_case_id", nullable = false)
    private DeathCase deathCase;

    private int month;
    private int year;

    private double amount;
    private LocalDate paymentDate;

    private String transactionId;
    private String filePath;

    @Enumerated(EnumType.STRING)
    private ReceiptStatus status;

    private Instant uploadedAt = Instant.now();
}

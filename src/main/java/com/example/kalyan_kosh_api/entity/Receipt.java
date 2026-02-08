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

    private double amount;
    private LocalDate paymentDate;

    // Reference name for the payment
    private String referenceName;

    // UTR (Unique Transaction Reference) number for payment verification
    @Column(name = "utr_number", length = 100)
    private String utrNumber;


    @Enumerated(EnumType.STRING)
    private ReceiptStatus status;

    private Instant uploadedAt = Instant.now();
}

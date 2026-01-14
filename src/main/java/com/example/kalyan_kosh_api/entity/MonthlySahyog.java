package com.example.kalyan_kosh_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "monthly_sahyog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlySahyog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate sahyogDate;  // Replaces month/year

    private int totalMembers;
    private int totalDeathCases;

    private double receivedAmount;

    @Enumerated(EnumType.STRING)
    private SahyogStatus status;

    private Instant generatedAt;
    private Instant frozenAt;
}

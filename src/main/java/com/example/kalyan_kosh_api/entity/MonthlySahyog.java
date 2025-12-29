package com.example.kalyan_kosh_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "monthly_sahyog",
        uniqueConstraints = @UniqueConstraint(columnNames = {"month", "year"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlySahyog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int month;   // 1–12
    private int year;    // 2025

    private int totalMembers;
    private int totalDeathCases;


    private double receivedAmount;

    @Enumerated(EnumType.STRING)
    private SahyogStatus status;

    private Instant generatedAt;
    private Instant frozenAt;
}

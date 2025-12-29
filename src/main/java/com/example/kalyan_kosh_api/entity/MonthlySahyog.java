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

    public Long getId() {
        return id;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public int getTotalMembers() {
        return totalMembers;
    }

    public int getTotalDeathCases() {
        return totalDeathCases;
    }

    public double getReceivedAmount() {
        return receivedAmount;
    }

    public SahyogStatus getStatus() {
        return status;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public Instant getFrozenAt() {
        return frozenAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setTotalMembers(int totalMembers) {
        this.totalMembers = totalMembers;
    }

    public void setTotalDeathCases(int totalDeathCases) {
        this.totalDeathCases = totalDeathCases;
    }

    public void setReceivedAmount(double receivedAmount) {
        this.receivedAmount = receivedAmount;
    }

    public void setStatus(SahyogStatus status) {
        this.status = status;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }

    public void setFrozenAt(Instant frozenAt) {
        this.frozenAt = frozenAt;
    }
}

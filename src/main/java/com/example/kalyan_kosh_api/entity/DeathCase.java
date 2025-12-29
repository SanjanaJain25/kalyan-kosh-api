package com.example.kalyan_kosh_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeathCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deceasedName;
    private String employeeCode;
    private String department;
    private String district;

    private String nomineeName;
    private String nomineeAccountNumber;
    private String nomineeIfsc;

    private int caseMonth;
    private int caseYear;

    private String createdBy;
    private String updatedBy;

    @Enumerated(EnumType.STRING)
    private DeathCaseStatus status;


    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}

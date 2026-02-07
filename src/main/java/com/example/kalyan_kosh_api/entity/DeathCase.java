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
public class DeathCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deceasedName;
    private String employeeCode;
    private String department;
    private String district;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String userImage; // Stores file path of user image

    // Nominee 1 Details
    private String nominee1Name;
    private String nominee1QrCode; // Stores file path of QR code image

    // Nominee 2 Details
    private String nominee2Name;
    private String nominee2QrCode; // Stores file path of QR code image

    // Death Case Certificates
    private String certificate1; // Stores file path of first certificate image

    // Account Details (using OneToOne relationship with AccountDetails entity)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "account1_id")
    private AccountDetails account1;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "account2_id")
    private AccountDetails account2;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "account3_id")
    private AccountDetails account3;

    private LocalDate caseDate;

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

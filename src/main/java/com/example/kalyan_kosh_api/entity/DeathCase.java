package com.example.kalyan_kosh_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
private String nominee1QrCode; // Old single QR field, keep for backward compatibility

@ElementCollection(fetch = FetchType.EAGER)
@CollectionTable(
        name = "death_case_nominee1_qr_codes",
        joinColumns = @JoinColumn(name = "death_case_id")
)
@OrderColumn(name = "qr_order")
@Column(name = "qr_code", length = 1000)
@Builder.Default
private List<String> nominee1QrCodes = new ArrayList<>();

@Column(length = 1000)
private String nominee1UpiLink;

// Nominee 2 Details
private String nominee2Name;
private String nominee2QrCode; // Old single QR field, keep for backward compatibility

@ElementCollection(fetch = FetchType.EAGER)
@CollectionTable(
        name = "death_case_nominee2_qr_codes",
        joinColumns = @JoinColumn(name = "death_case_id")
)
@OrderColumn(name = "qr_order")
@Column(name = "qr_code", length = 1000)
@Builder.Default
private List<String> nominee2QrCodes = new ArrayList<>();

@Column(length = 1000)
private String nominee2UpiLink;
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


    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}

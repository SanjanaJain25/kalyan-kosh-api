package com.example.kalyan_kosh_api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.Instant;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User {
    @Id
    @Column(length = 20)
    private String id;

    private String name;
    private String surname;
    private String fatherName;          // Added father name
    private String countryCode;
    private String phoneNumber;
    private String email;
    private String gender;
    private String maritalStatus;

    // Removed username field

    private String mobileNumber;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordHash; // store hashed (not returned in API)

    private String homeAddress;
    private LocalDate dateOfBirth;
    private LocalDate joiningDate;      // Added joining date
    private LocalDate retirementDate;   // Added retirement date
    private String schoolOfficeName;    // पदस्थ स्कूल/कार्यालय का नाम
    private String sankulName;          // संकुल का नाम
    private String department;
    @Column(unique = true, nullable = false)
    private String departmentUniqueId;

    // Department location hierarchy - using entity relationships
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_state_id")
    private State departmentState;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_sambhag_id")
    private Sambhag departmentSambhag;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_district_id")
    private District departmentDistrict;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_block_id")
    private Block departmentBlock;

    private String nominee1Name;
    private String nominee1Relation;
    private String nominee2Name;
    private String nominee2Relation;
    private boolean acceptedTerms;

    @Enumerated(EnumType.STRING)
    private Role role = Role.ROLE_USER;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() { updatedAt = Instant.now(); }
}

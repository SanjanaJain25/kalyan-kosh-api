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

    // Basic Info
    private String name;
    private String surname;
    private String fatherName;
    private String gender;
    private String maritalStatus;
    private LocalDate dateOfBirth;
    private String countryCode;
    private String mobileNumber;
    private String email;

    // Address details
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
    private String homeAddress;
    private Integer pincode;

    //Professional Details
    private String department;
    private String schoolOfficeName;    // पदस्थ स्कूल/कार्यालय का नाम
    @Column(unique = true, nullable = true)  // ✅ Made nullable - users may not have this during registration
    private String departmentUniqueId;
    private String sankulName;          // संकुल का नाम
    private LocalDate joiningDate;      // Added joining date
    private LocalDate retirementDate;   // Added retirement date

    //Nominee Details
    private String nominee1Name;
    private String nominee1Relation;
    private String nominee2Name;
    private String nominee2Relation;

    //Account Verification
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordHash; // store hashed (not returned in API)

    @Enumerated(EnumType.STRING)
    private Role role = Role.ROLE_USER;

    @Column(updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}

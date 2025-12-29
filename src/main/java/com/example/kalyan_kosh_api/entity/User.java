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
    private String countryCode;
    private String phoneNumber;
    private String email;
    private String gender;
    private String maritalStatus;

    @Column(unique = true, nullable = false)
    private String username;

    private String mobileNumber;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordHash; // store hashed (not returned in API)

    private String homeAddress;
    private LocalDate dateOfBirth;
    private String schoolOfficeName;
    private String department;
    private String departmentUniqueId;
    private String departmentDistrict;
    private String departmentBlock;
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

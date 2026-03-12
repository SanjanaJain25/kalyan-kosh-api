package com.example.kalyan_kosh_api.dto;

import com.example.kalyan_kosh_api.entity.Role;
import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO for User response - contains user details without sensitive information
 */
public class UserResponse {
    private String id;
    private String name;
    private String surname;
    private String fatherName;          // Added father name
    // Removed username field
    private String email;
    private String gender;
    private String maritalStatus;
    private String homeAddress;
    private Integer pincode;            // Added pincode
    private LocalDate dateOfBirth;
    private LocalDate joiningDate;      // Added joining date
    private LocalDate retirementDate;   // Added retirement date
    private String schoolOfficeName;    // पदस्थ स्कूल/कार्यालय का नाम
    private String sankulName;          // संकुल का नाम
    private String department;
    private String departmentUniqueId;
    private String departmentState;
    private String departmentSambhag;
    private String departmentDistrict;
    private String departmentBlock;
    private String nominee1Name;
    private String nominee1Relation;
    private String nominee2Name;
    private String nominee2Relation;
    private Role role;
    private Instant createdAt;

    // Default constructor
    public UserResponse() {}

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }


    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }

    public String getHomeAddress() { return homeAddress; }
    public void setHomeAddress(String homeAddress) { this.homeAddress = homeAddress; }

    public Integer getPincode() { return pincode; }
    public void setPincode(Integer pincode) { this.pincode = pincode; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

    public LocalDate getRetirementDate() { return retirementDate; }
    public void setRetirementDate(LocalDate retirementDate) { this.retirementDate = retirementDate; }

    public String getSchoolOfficeName() { return schoolOfficeName; }
    public void setSchoolOfficeName(String schoolOfficeName) { this.schoolOfficeName = schoolOfficeName; }

    public String getSankulName() { return sankulName; }
    public void setSankulName(String sankulName) { this.sankulName = sankulName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDepartmentUniqueId() { return departmentUniqueId; }
    public void setDepartmentUniqueId(String departmentUniqueId) { this.departmentUniqueId = departmentUniqueId; }

    public String getDepartmentState() { return departmentState; }
    public void setDepartmentState(String departmentState) { this.departmentState = departmentState; }

    public String getDepartmentSambhag() { return departmentSambhag; }
    public void setDepartmentSambhag(String departmentSambhag) { this.departmentSambhag = departmentSambhag; }

    public String getDepartmentDistrict() { return departmentDistrict; }
    public void setDepartmentDistrict(String departmentDistrict) { this.departmentDistrict = departmentDistrict; }

    public String getDepartmentBlock() { return departmentBlock; }
    public void setDepartmentBlock(String departmentBlock) { this.departmentBlock = departmentBlock; }

    public String getNominee1Name() { return nominee1Name; }
    public void setNominee1Name(String nominee1Name) { this.nominee1Name = nominee1Name; }

    public String getNominee1Relation() { return nominee1Relation; }
    public void setNominee1Relation(String nominee1Relation) { this.nominee1Relation = nominee1Relation; }

    public String getNominee2Name() { return nominee2Name; }
    public void setNominee2Name(String nominee2Name) { this.nominee2Name = nominee2Name; }

    public String getNominee2Relation() { return nominee2Relation; }
    public void setNominee2Relation(String nominee2Relation) { this.nominee2Relation = nominee2Relation; }


    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

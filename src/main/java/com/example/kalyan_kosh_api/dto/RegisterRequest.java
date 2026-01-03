package com.example.kalyan_kosh_api.dto;

/**
 * DTO for registration request - fields match your User entity.
 * Note: dateOfBirth, joiningDate, retirementDate are accepted as ISO string (yyyy-MM-dd) and parsed in service.
 */
public class RegisterRequest {
    private String name;
    private String surname;
    private String fatherName;          // Added father name
    private String countryCode;
    private String phoneNumber;
    private String mobileNumber;
    private String email;
    private String gender;
    private String maritalStatus;
    // Removed username field - now using email for authentication
    private String password;
    private String homeAddress;
    private String dateOfBirth;         // "1999-09-11"
    private String joiningDate;         // Added joining date
    private String retirementDate;      // Added retirement date
    private String schoolOfficeName;    // पदस्थ स्कूल/कार्यालय का नाम
    private String sankulName;          // संकुल का नाम
    private String department;
    private String departmentUniqueId;
    private String departmentState;      // "Madhya Pradesh"
    private String departmentSambhag;    // "Chambal"
    private String departmentDistrict;
    private String departmentBlock;
    private String nominee1Name;
    private String nominee1Relation;
    private String nominee2Name;
    private String nominee2Relation;
    private boolean acceptedTerms;

    // Default constructor
    public RegisterRequest() {}

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getHomeAddress() { return homeAddress; }
    public void setHomeAddress(String homeAddress) { this.homeAddress = homeAddress; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getJoiningDate() { return joiningDate; }
    public void setJoiningDate(String joiningDate) { this.joiningDate = joiningDate; }

    public String getRetirementDate() { return retirementDate; }
    public void setRetirementDate(String retirementDate) { this.retirementDate = retirementDate; }

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

    public boolean isAcceptedTerms() { return acceptedTerms; }
    public void setAcceptedTerms(boolean acceptedTerms) { this.acceptedTerms = acceptedTerms; }
}

package com.example.kalyan_kosh_api.dto;

import java.util.List;

public class PublicUserResponse {

    private String id;
    private String name;
    private String surname;

    private String department;
    private String departmentUniqueId;
    private String departmentState;
    private String departmentSambhag;
    private String departmentDistrict;
    private String departmentBlock;

    private String schoolOfficeName;

    private Long assignedDeathCaseId;
    private String assignedDeathCaseName;

    private String allocatedQrCode;
    private List<String> nominee1QrCodes;
    private List<String> nominee2QrCodes;

    private Boolean utrUploaded;

    public PublicUserResponse() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

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

    public String getSchoolOfficeName() { return schoolOfficeName; }
    public void setSchoolOfficeName(String schoolOfficeName) { this.schoolOfficeName = schoolOfficeName; }

    public Long getAssignedDeathCaseId() { return assignedDeathCaseId; }
    public void setAssignedDeathCaseId(Long assignedDeathCaseId) { this.assignedDeathCaseId = assignedDeathCaseId; }

    public String getAssignedDeathCaseName() { return assignedDeathCaseName; }
    public void setAssignedDeathCaseName(String assignedDeathCaseName) { this.assignedDeathCaseName = assignedDeathCaseName; }

    public String getAllocatedQrCode() { return allocatedQrCode; }
    public void setAllocatedQrCode(String allocatedQrCode) { this.allocatedQrCode = allocatedQrCode; }

    public List<String> getNominee1QrCodes() { return nominee1QrCodes; }
    public void setNominee1QrCodes(List<String> nominee1QrCodes) { this.nominee1QrCodes = nominee1QrCodes; }

    public List<String> getNominee2QrCodes() { return nominee2QrCodes; }
    public void setNominee2QrCodes(List<String> nominee2QrCodes) { this.nominee2QrCodes = nominee2QrCodes; }

    public Boolean getUtrUploaded() { return utrUploaded; }
    public void setUtrUploaded(Boolean utrUploaded) { this.utrUploaded = utrUploaded; }
}
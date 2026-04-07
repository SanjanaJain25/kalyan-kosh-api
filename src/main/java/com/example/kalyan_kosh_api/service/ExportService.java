package com.example.kalyan_kosh_api.service;
import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.dto.AdminReceiptResponse;
import com.example.kalyan_kosh_api.dto.AdminUserResponse;
import com.example.kalyan_kosh_api.dto.DeathCaseResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import com.example.kalyan_kosh_api.repository.UserRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import com.example.kalyan_kosh_api.entity.InsuranceInquiry;
import com.example.kalyan_kosh_api.repository.InsuranceInquiryRepository;
import org.springframework.beans.factory.annotation.Value;
import com.example.kalyan_kosh_api.entity.Role;
import java.util.stream.Collectors;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import com.example.kalyan_kosh_api.dto.DonorResponse;
import com.example.kalyan_kosh_api.dto.UserResponse;

@Service
public class ExportService {
private final UserRepository userRepository;
private final SystemSettingService systemSettingService;
private final InsuranceInquiryRepository insuranceInquiryRepository;
private final EmailService emailService;

@Value("${insurance.export.recipient}")
private String insuranceExportRecipient;

public ExportService(
        UserRepository userRepository,
        SystemSettingService systemSettingService,
        InsuranceInquiryRepository insuranceInquiryRepository,
        EmailService emailService
) {
    this.userRepository = userRepository;
    this.systemSettingService = systemSettingService;
    this.insuranceInquiryRepository = insuranceInquiryRepository;
    this.emailService = emailService;
}

public List<AdminUserResponse> getUsersForExportByMonthYear(int month, int year) {
    List<User> users = userRepository.findAllByCreatedMonthAndYear(month, year);

    return users.stream().map(user -> AdminUserResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .surname(user.getSurname())
            .fatherName(user.getFatherName())
            .email(user.getEmail())
           .mobileNumber(systemSettingService.isExportMobileNumberEnabled() ? user.getMobileNumber() : null)
            .dateOfBirth(user.getDateOfBirth())
            .departmentState(user.getDepartmentState() != null ? user.getDepartmentState().getName() : null)
            .departmentSambhag(user.getDepartmentSambhag() != null ? user.getDepartmentSambhag().getName() : null)
            .departmentDistrict(user.getDepartmentDistrict() != null ? user.getDepartmentDistrict().getName() : null)
            .departmentBlock(user.getDepartmentBlock() != null ? user.getDepartmentBlock().getName() : null)
            .department(user.getDepartment())
            .departmentUniqueId(user.getDepartmentUniqueId())
            .schoolOfficeName(user.getSchoolOfficeName())
            .sankulName(user.getSankulName())
            .homeAddress(user.getHomeAddress())
            .pincode(user.getPincode())
            .joiningDate(user.getJoiningDate())
            .retirementDate(user.getRetirementDate())
            .role(user.getRole())
            .status(user.getStatus())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build()
    ).toList();
}
public List<AdminUserResponse> getAllUsersForExport() {
    List<User> users = userRepository.findAllWithLocations();

    return users.stream()
            .filter(user -> !(user.getId().equals("PMUMS202502") && user.getRole() == Role.ROLE_SUPERADMIN))
            .map(user -> AdminUserResponse.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .surname(user.getSurname())
                    .fatherName(user.getFatherName())
                    .email(user.getEmail())
                    .mobileNumber(systemSettingService.isExportMobileNumberEnabled() ? user.getMobileNumber() : null)
                    .dateOfBirth(user.getDateOfBirth())
                    .departmentState(user.getDepartmentState() != null ? user.getDepartmentState().getName() : null)
                    .departmentSambhag(user.getDepartmentSambhag() != null ? user.getDepartmentSambhag().getName() : null)
                    .departmentDistrict(user.getDepartmentDistrict() != null ? user.getDepartmentDistrict().getName() : null)
                    .departmentBlock(user.getDepartmentBlock() != null ? user.getDepartmentBlock().getName() : null)
                    .department(user.getDepartment())
                    .departmentUniqueId(user.getDepartmentUniqueId())
                    .schoolOfficeName(user.getSchoolOfficeName())
                    .sankulName(user.getSankulName())
                    .homeAddress(user.getHomeAddress())
                    .pincode(user.getPincode())
                    .joiningDate(user.getJoiningDate())
                    .retirementDate(user.getRetirementDate())
                    .role(user.getRole())
                    .status(user.getStatus())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build()
            )
            .toList();
}

public void exportAllUsersCsvStream(OutputStream outputStream) throws IOException {
    int page = 0;
    int size = 2000;
    boolean hasMore = true;

    try (BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), 64 * 1024)) {

        // Header row
        writer.write("पंजीकरण संख्या (User ID),"
                + "नाम (Name),"
                + "उपनाम (Surname),"
                + "पिता का नाम (Father Name),"
                + "ईमेल (Email),"
                + "मोबाइल नंबर (Mobile),"
                + "जन्म तिथि (DOB),"
                + "राज्य (State),"
                + "संभाग (Sambhag/Division),"
                + "जिला (District),"
                + "ब्लॉक (Block),"
                + "विभाग (Department),"
                + "विभाग आईडी (Dept ID),"
                + "स्कूल/कार्यालय का नाम (School/Office),"
                + "संकुल का नाम (Sankul),"
                + "घर का पता (Home Address),"
                + "पिनकोड (Pincode),"
                + "नियुक्ति तिथि (Joining Date),"
                + "सेवानिवृत्ति तिथि (Retirement Date),"
                + "भूमिका (Role),"
                + "स्थिति (Status),"
                + "पंजीकरण तिथि (Created At),"
                + "अंतिम अपडेट (Updated At)");
        writer.newLine();

        while (hasMore) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

            Page<User> userPage = userRepository.findExportUsersPaged(
                    "PMUMS202502",
                    Role.ROLE_SUPERADMIN,
                    pageable
            );

            for (User user : userPage.getContent()) {
                writer.write(csv(user.getId()));
                writer.write(",");
                writer.write(csv(user.getName()));
                writer.write(",");
                writer.write(csv(user.getSurname()));
                writer.write(",");
                writer.write(csv(user.getFatherName()));
                writer.write(",");
                writer.write(csv(user.getEmail()));
                writer.write(",");
                writer.write(csv(systemSettingService.isExportMobileNumberEnabled() ? user.getMobileNumber() : ""));
                writer.write(",");
                writer.write(csv(user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : ""));
                writer.write(",");
                writer.write(csv(user.getDepartmentState() != null ? user.getDepartmentState().getName() : ""));
                writer.write(",");
                writer.write(csv(user.getDepartmentSambhag() != null ? user.getDepartmentSambhag().getName() : ""));
                writer.write(",");
                writer.write(csv(user.getDepartmentDistrict() != null ? user.getDepartmentDistrict().getName() : ""));
                writer.write(",");
                writer.write(csv(user.getDepartmentBlock() != null ? user.getDepartmentBlock().getName() : ""));
                writer.write(",");
                writer.write(csv(user.getDepartment()));
                writer.write(",");
                writer.write(csv(user.getDepartmentUniqueId()));
                writer.write(",");
                writer.write(csv(user.getSchoolOfficeName()));
                writer.write(",");
                writer.write(csv(user.getSankulName()));
                writer.write(",");
                writer.write(csv(user.getHomeAddress()));
                writer.write(",");
                writer.write(csv(user.getPincode() != null ? user.getPincode().toString() : ""));
                writer.write(",");
                writer.write(csv(user.getJoiningDate() != null ? user.getJoiningDate().toString() : ""));
                writer.write(",");
                writer.write(csv(user.getRetirementDate() != null ? user.getRetirementDate().toString() : ""));
                writer.write(",");
                writer.write(csv(user.getRole() != null ? user.getRole().name() : ""));
                writer.write(",");
                writer.write(csv(user.getStatus() != null ? user.getStatus().name() : ""));
                writer.write(",");
                writer.write(csv(user.getCreatedAt() != null ? user.getCreatedAt().toString() : ""));
                writer.write(",");
                writer.write(csv(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : ""));
                writer.newLine();
            }

            writer.flush(); // important for streaming big files
            hasMore = userPage.hasNext();
            page++;
        }
    }
}
private String csv(String value) {
    if (value == null) {
        return "";
    }

    if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    return value;
}

public Map<String, Object> exportInsuranceInquiriesAndSendEmail() {
    try {
        List<InsuranceInquiry> inquiries = insuranceInquiryRepository.findAllByOrderByCreatedAtDesc();
        byte[] excelBytes = exportInsuranceInquiriesExcel(inquiries);

        String subject = "Insurance Inquiries Export";
        String body = "Please find attached the latest insurance inquiries export file.";

        emailService.sendEmailWithAttachment(
                insuranceExportRecipient,
                subject,
                body,
                excelBytes,
                "insurance_inquiries.xlsx"
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Insurance inquiries exported and emailed successfully.");
        response.put("count", inquiries.size());

        return response;
    } catch (Exception e) {
        throw new RuntimeException("Failed to export insurance inquiries and send email", e);
    }
}

public byte[] exportInsuranceInquiriesExcel(List<InsuranceInquiry> inquiries) throws IOException {
    try (Workbook workbook = new XSSFWorkbook()) {
        Sheet sheet = workbook.createSheet("Insurance Inquiries");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "ID", "Name", "District", "Mobile Number", "Created At"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")
                .withZone(ZoneId.systemDefault());

        int rowNum = 1;
        for (InsuranceInquiry inquiry : inquiries) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(inquiry.getId() != null ? inquiry.getId() : 0);
            row.createCell(1).setCellValue(inquiry.getName() != null ? inquiry.getName() : "");
            row.createCell(2).setCellValue(inquiry.getDistrict() != null ? inquiry.getDistrict() : "");
            row.createCell(3).setCellValue(inquiry.getMobileNumber() != null ? inquiry.getMobileNumber() : "");
            row.createCell(4).setCellValue(
                    inquiry.getCreatedAt() != null ? formatter.format(inquiry.getCreatedAt()) : ""
            );
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return outputStream.toByteArray();
    }
}

public byte[] exportUsersExcel(List<AdminUserResponse> users) throws IOException {
    try (Workbook workbook = new XSSFWorkbook()) {
        Sheet sheet = workbook.createSheet("Users");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "User ID", "Name", "Surname", "Father Name", "Email", "Mobile",
                "DOB", "State", "Sambhag", "District", "Block", "Department",
                "Department ID", "School/Office", "Sankul", "Home Address",
                "Pincode", "Joining Date", "Retirement Date", "Role", "Status",
                "Created At", "Updated At"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (AdminUserResponse user : users) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(user.getId() != null ? user.getId() : "");
            row.createCell(1).setCellValue(user.getName() != null ? user.getName() : "");
            row.createCell(2).setCellValue(user.getSurname() != null ? user.getSurname() : "");
            row.createCell(3).setCellValue(user.getFatherName() != null ? user.getFatherName() : "");
            row.createCell(4).setCellValue(user.getEmail() != null ? user.getEmail() : "");
            row.createCell(5).setCellValue(user.getMobileNumber() != null ? user.getMobileNumber() : "");
            row.createCell(6).setCellValue(user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : "");
            row.createCell(7).setCellValue(user.getDepartmentState() != null ? user.getDepartmentState() : "");
            row.createCell(8).setCellValue(user.getDepartmentSambhag() != null ? user.getDepartmentSambhag() : "");
            row.createCell(9).setCellValue(user.getDepartmentDistrict() != null ? user.getDepartmentDistrict() : "");
            row.createCell(10).setCellValue(user.getDepartmentBlock() != null ? user.getDepartmentBlock() : "");
            row.createCell(11).setCellValue(user.getDepartment() != null ? user.getDepartment() : "");
            row.createCell(12).setCellValue(user.getDepartmentUniqueId() != null ? user.getDepartmentUniqueId() : "");
            row.createCell(13).setCellValue(user.getSchoolOfficeName() != null ? user.getSchoolOfficeName() : "");
            row.createCell(14).setCellValue(user.getSankulName() != null ? user.getSankulName() : "");
            row.createCell(15).setCellValue(user.getHomeAddress() != null ? user.getHomeAddress() : "");
            row.createCell(16).setCellValue(user.getPincode() != null ? user.getPincode().toString() : "");
            row.createCell(17).setCellValue(user.getJoiningDate() != null ? user.getJoiningDate().toString() : "");
            row.createCell(18).setCellValue(user.getRetirementDate() != null ? user.getRetirementDate().toString() : "");
            row.createCell(19).setCellValue(user.getRole() != null ? user.getRole().name() : "");
            row.createCell(20).setCellValue(user.getStatus() != null ? user.getStatus().name() : "");
            row.createCell(21).setCellValue(user.getCreatedAt() != null ? user.getCreatedAt().toString() : "");
            row.createCell(22).setCellValue(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : "");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return outputStream.toByteArray();
    }
}

    public String exportCsv(List<AdminReceiptResponse> data) {

        StringBuilder sb = new StringBuilder();
        sb.append("RegNo,Name,District,Block,Department,PaymentDate,Amount\n");

        for (AdminReceiptResponse r : data) {
            sb.append(r.getRegNo()).append(",")
                    .append(r.getName()).append(",")
                    .append(r.getDistrict()).append(",")
                    .append(r.getBlock()).append(",")
                    .append(r.getDepartment()).append(",")
                    .append(r.getPaymentDate()).append(",")
                    .append(r.getAmount()).append("\n");
        }
        return sb.toString();
    }
    
    /**
     * Export users data to CSV format with proper UTF-8 encoding
     */
    public String exportUsersCsv(List<AdminUserResponse> users) {
        StringBuilder sb = new StringBuilder();
        
        // CSV Header - with clear column names
        sb.append("पंजीकरण संख्या (User ID),")
          .append("नाम (Name),")
          .append("उपनाम (Surname),")
          .append("पिता का नाम (Father Name),")
          .append("ईमेल (Email),")
          .append("मोबाइल नंबर (Mobile),")
          .append("जन्म तिथि (DOB),")
          .append("राज्य (State),")
          .append("संभाग (Sambhag/Division),")
          .append("जिला (District),")
          .append("ब्लॉक (Block),")
          .append("विभाग (Department),")
          .append("विभाग आईडी (Dept ID),")
          .append("स्कूल/कार्यालय का नाम (School/Office),")
          .append("संकुल का नाम (Sankul),")
          .append("घर का पता (Home Address),")
          .append("पिनकोड (Pincode),")
          .append("नियुक्ति तिथि (Joining Date),")
          .append("सेवानिवृत्ति तिथि (Retirement Date),")
          .append("भूमिका (Role),")
          .append("स्थिति (Status),")
          .append("पंजीकरण तिथि (Created At),")
          .append("अंतिम अपडेट (Updated At)\n");

        // CSV Data
        for (AdminUserResponse user : users) {
            sb.append(escapeCSV(user.getId())).append(",")
              .append(escapeCSV(user.getName())).append(",")
              .append(escapeCSV(user.getSurname())).append(",")
              .append(escapeCSV(user.getFatherName())).append(",")
              .append(escapeCSV(user.getEmail())).append(",")
              .append(escapeCSV(user.getMobileNumber())).append(",")
              .append(user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : "").append(",")
              .append(escapeCSV(user.getDepartmentState())).append(",")
              .append(escapeCSV(user.getDepartmentSambhag())).append(",")
              .append(escapeCSV(user.getDepartmentDistrict())).append(",")
              .append(escapeCSV(user.getDepartmentBlock())).append(",")
              .append(escapeCSV(user.getDepartment())).append(",")
              .append(escapeCSV(user.getDepartmentUniqueId())).append(",")
              .append(escapeCSV(user.getSchoolOfficeName())).append(",")
              .append(escapeCSV(user.getSankulName())).append(",")
              .append(escapeCSV(user.getHomeAddress())).append(",")
              .append(user.getPincode() != null ? user.getPincode().toString() : "").append(",")
              .append(user.getJoiningDate() != null ? user.getJoiningDate().toString() : "").append(",")
              .append(user.getRetirementDate() != null ? user.getRetirementDate().toString() : "").append(",")
              .append(user.getRole() != null ? user.getRole().name() : "").append(",")
              .append(user.getStatus() != null ? user.getStatus().name() : "").append(",")
              .append(user.getCreatedAt() != null ? user.getCreatedAt().toString() : "").append(",")
              .append(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : "")
              .append("\n");
        }
        
        return sb.toString();
    }
    
    public String exportDonorsCsv(List<DonorResponse> donors) {
    StringBuilder sb = new StringBuilder();

    sb.append("Registration Number,Name,Department,State,Sambhag,District,Block,School Name,Beneficiary,Receipt Upload Date\n");

    for (DonorResponse donor : donors) {
        sb.append(escapeCSV(donor.getRegistrationNumber())).append(",")
          .append(escapeCSV(donor.getName())).append(",")
          .append(escapeCSV(donor.getDepartment())).append(",")
          .append(escapeCSV(donor.getState())).append(",")
          .append(escapeCSV(donor.getSambhag())).append(",")
          .append(escapeCSV(donor.getDistrict())).append(",")
          .append(escapeCSV(donor.getBlock())).append(",")
          .append(escapeCSV(donor.getSchoolName())).append(",")
          .append(escapeCSV(donor.getBeneficiary())).append(",")
          .append(donor.getReceiptUploadDate() != null ? donor.getReceiptUploadDate().toString() : "")
          .append("\n");
    }

    return sb.toString();
}

public String exportNonDonorsCsv(List<UserResponse> users) {
    StringBuilder sb = new StringBuilder();

    sb.append("User ID,Name,Surname,Father Name,Email,Department,State,Sambhag,District,Block,School/Office,Mobile,Status\n");

    for (UserResponse user : users) {
        sb.append(escapeCSV(user.getId())).append(",")
          .append(escapeCSV(user.getName())).append(",")
          .append(escapeCSV(user.getSurname())).append(",")
          .append(escapeCSV(user.getFatherName())).append(",")
          .append(escapeCSV(user.getEmail())).append(",")
          .append(escapeCSV(user.getDepartment())).append(",")
          .append(escapeCSV(user.getDepartmentState())).append(",")
          .append(escapeCSV(user.getDepartmentSambhag())).append(",")
          .append(escapeCSV(user.getDepartmentDistrict())).append(",")
          .append(escapeCSV(user.getDepartmentBlock())).append(",")
          .append(escapeCSV(user.getSchoolOfficeName())).append(",")
          .append(escapeCSV(user.getMobileNumber())).append(",")
          .append("NON_DONOR")
          .append("\n");
    }

    return sb.toString();
}

public String exportPendingProfilesCsv(List<UserResponse> users) {
    StringBuilder sb = new StringBuilder();

    sb.append("User ID,Name,Surname,Father Name,Email,Mobile,Department,State,Sambhag,District,Block,School/Office,Pending Reason\n");

    for (UserResponse user : users) {
        sb.append(escapeCSV(user.getId())).append(",")
          .append(escapeCSV(user.getName())).append(",")
          .append(escapeCSV(user.getSurname())).append(",")
          .append(escapeCSV(user.getFatherName())).append(",")
          .append(escapeCSV(user.getEmail())).append(",")
          .append(escapeCSV(user.getMobileNumber())).append(",")
          .append(escapeCSV(user.getDepartment())).append(",")
          .append(escapeCSV(user.getDepartmentState())).append(",")
          .append(escapeCSV(user.getDepartmentSambhag())).append(",")
          .append(escapeCSV(user.getDepartmentDistrict())).append(",")
          .append(escapeCSV(user.getDepartmentBlock())).append(",")
          .append(escapeCSV(user.getSchoolOfficeName())).append(",")
          .append("Profile Incomplete")
          .append("\n");
    }

    return sb.toString();
}
    /**
     * Escape CSV values to handle commas, quotes, and newlines
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        
        // If value contains comma, quote, or newline, wrap in quotes and escape internal quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }

    /**
     * Export death cases data to Excel format
     */
    public byte[] exportDeathCasesExcel(List<DeathCaseResponse> deathCases) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Death Cases");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "ID", "Deceased Name", "Employee Code", "Department", "District",
                "Description", "Nominee 1 Name", "Nominee 2 Name",
                "Case Date", "Status", "Created By", "Created At"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            int rowNum = 1;
            for (DeathCaseResponse dc : deathCases) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(dc.getId() != null ? dc.getId() : 0);
                row.createCell(1).setCellValue(dc.getDeceasedName() != null ? dc.getDeceasedName() : "");
                row.createCell(2).setCellValue(dc.getEmployeeCode() != null ? dc.getEmployeeCode() : "");
                row.createCell(3).setCellValue(dc.getDepartment() != null ? dc.getDepartment() : "");
                row.createCell(4).setCellValue(dc.getDistrict() != null ? dc.getDistrict() : "");
                row.createCell(5).setCellValue(dc.getDescription() != null ? dc.getDescription() : "");
                row.createCell(6).setCellValue(dc.getNominee1Name() != null ? dc.getNominee1Name() : "");
                row.createCell(7).setCellValue(dc.getNominee2Name() != null ? dc.getNominee2Name() : "");
                row.createCell(8).setCellValue(dc.getCaseDate() != null ? dc.getCaseDate().toString() : "");
                row.createCell(9).setCellValue(dc.getStatus() != null ? dc.getStatus().name() : "");
                row.createCell(10).setCellValue(dc.getCreatedBy() != null ? dc.getCreatedBy() : "");
                row.createCell(11).setCellValue(dc.getCreatedAt() != null ? dc.getCreatedAt().toString() : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    /**
     * Export receipts to Excel format
     */
    public byte[] exportReceiptsExcel(List<AdminReceiptResponse> receipts) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Receipts");
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Registration No", "Name", "Sambhag", "District", "Block",
                "Department", "Payment Date", "Amount"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            int rowNum = 1;
            for (AdminReceiptResponse receipt : receipts) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(receipt.getRegNo() != null ? receipt.getRegNo() : "");
                row.createCell(1).setCellValue(receipt.getName() != null ? receipt.getName() : "");
                row.createCell(2).setCellValue(receipt.getSambhag() != null ? receipt.getSambhag() : "");
                row.createCell(3).setCellValue(receipt.getDistrict() != null ? receipt.getDistrict() : "");
                row.createCell(4).setCellValue(receipt.getBlock() != null ? receipt.getBlock() : "");
                row.createCell(5).setCellValue(receipt.getDepartment() != null ? receipt.getDepartment() : "");
                row.createCell(6).setCellValue(receipt.getPaymentDate() != null ? receipt.getPaymentDate().toString() : "");
                row.createCell(7).setCellValue(receipt.getAmount());
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}

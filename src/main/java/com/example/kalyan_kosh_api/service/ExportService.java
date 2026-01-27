package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.AdminReceiptResponse;
import com.example.kalyan_kosh_api.dto.AdminUserResponse;
import com.example.kalyan_kosh_api.dto.DeathCaseResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExportService {

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
        
        // CSV Header
        sb.append("ID,Name,Surname,FatherName,Email,MobileNumber,DateOfBirth,")
          .append("DepartmentState,DepartmentSambhag,DepartmentDistrict,DepartmentBlock,")
          .append("Department,DepartmentUniqueId,SchoolOfficeName,SankulName,")
          .append("HomeAddress,Pincode,JoiningDate,Role,Status,CreatedAt,UpdatedAt\n");
        
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
              .append(user.getRole() != null ? user.getRole().name() : "").append(",")
              .append(user.getStatus() != null ? user.getStatus().name() : "").append(",")
              .append(user.getCreatedAt() != null ? user.getCreatedAt().toString() : "").append(",")
              .append(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : "")
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

package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.AdminUserListResponse;
import com.example.kalyan_kosh_api.dto.AdminUserResponse;
import com.example.kalyan_kosh_api.dto.UpdateUserRoleRequest;
import com.example.kalyan_kosh_api.entity.Role;
import com.example.kalyan_kosh_api.entity.UserStatus;
import com.example.kalyan_kosh_api.service.AdminUserManagementService;
import com.example.kalyan_kosh_api.service.ExportService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin User Management Controller
 * Handles all admin operations for user management including:
 * - User listing with filters and pagination
 * - Block/unblock users
 * - Delete users (soft delete)
 * - Update user roles
 * - Export all users to CSV file
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class AdminUserManagementController {

    private final AdminUserManagementService adminUserService;
    private final ExportService exportService;

    public AdminUserManagementController(AdminUserManagementService adminUserService, 
                                       ExportService exportService) {
        this.adminUserService = adminUserService;
        this.exportService = exportService;
    }

    /**
     * Get all users with pagination and filters
     * 
     * @param page Page number (0-based)
     * @param size Page size (default 20)
     * @param name Filter by name (partial match)
     * @param email Filter by email (partial match)
     * @param mobileNumber Filter by mobile number (partial match)
     * @param role Filter by role
     * @param status Filter by status
     * @param sambhag Filter by sambhag (partial match)
     * @param district Filter by district (partial match)
     * @param block Filter by block (partial match)
     * @return Paginated list of users
     */
    @GetMapping
    public ResponseEntity<AdminUserListResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String mobileNumber,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String sambhag,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String block) {
        
        AdminUserListResponse response = adminUserService.getAllUsers(
                page, size, name, email, mobileNumber, role, status, sambhag, district, block);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdminUserResponse> getUserById(@PathVariable String id) {
        AdminUserResponse user = adminUserService.getUserByIdResponse(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Block a user
     */
    @PutMapping("/{id}/block")
    public ResponseEntity<Map<String, String>> blockUser(@PathVariable String id) {
        adminUserService.blockUser(id);
        return ResponseEntity.ok(Map.of(
                "message", "User blocked successfully",
                "userId", id,
                "action", "BLOCKED"
        ));
    }

    /**
     * Unblock a user
     */
    @PutMapping("/{id}/unblock")
    public ResponseEntity<Map<String, String>> unblockUser(@PathVariable String id) {
        adminUserService.unblockUser(id);
        return ResponseEntity.ok(Map.of(
                "message", "User unblocked successfully",
                "userId", id,
                "action", "UNBLOCKED"
        ));
    }

    /**
     * Delete a user (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.ok(Map.of(
                "message", "User deleted successfully",
                "userId", id,
                "action", "DELETED"
        ));
    }

    /**
     * Export all users to CSV file
     * Only admin can download this file
     * 
     * @param role Filter by role (optional)
     * @param status Filter by status (optional) 
     * @param sambhag Filter by sambhag (optional)
     * @param district Filter by district (optional)
     * @param block Filter by block (optional)
     * @param response HTTP response to write CSV data
     */
    @GetMapping("/export")
    public void exportUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String sambhag,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String block,
            HttpServletResponse response) throws Exception {
        
        // Get all users with filters (use a large page size to get all users)
        AdminUserListResponse allUsers = adminUserService.getAllUsers(
                0, Integer.MAX_VALUE, null, null, null, role, status, sambhag, district, block);
        
        // Generate CSV content
        String csvContent = exportService.exportUsersCsv(allUsers.getUsers());
        
        // Set response headers for file download with proper UTF-8 encoding
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", 
                "attachment; filename*=UTF-8''users_export_" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
        
        // Write UTF-8 BOM and CSV content using OutputStream for better encoding control
        try (var outputStream = response.getOutputStream()) {
            // Write UTF-8 BOM bytes explicitly
            outputStream.write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});
            // Write CSV content as UTF-8 bytes
            outputStream.write(csvContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            outputStream.flush();
        }
    }
}
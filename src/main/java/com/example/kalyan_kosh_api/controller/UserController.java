package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.PageResponse;
import com.example.kalyan_kosh_api.dto.RegisterRequest;
import com.example.kalyan_kosh_api.dto.UpdatePasswordRequest;
import com.example.kalyan_kosh_api.dto.UpdateUserRequest;
import com.example.kalyan_kosh_api.dto.UserResponse;
import com.example.kalyan_kosh_api.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // REGISTER USER
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            UserResponse user = userService.register(req);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Registration failed: " + e.getMessage());
        }
    }

    // GET USER BY ID
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // GET ALL USERS (without pagination)
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * GET ALL USERS WITH PAGINATION - 20 records per page by default
     * Sorted by insertion order (createdAt ASC)
     *
     * Usage: GET /api/users/paginated?page=0&size=20
     */
    @GetMapping("/paginated")
    public ResponseEntity<PageResponse<UserResponse>> getAllUsersPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "250") int size) {
        PageResponse<UserResponse> response = userService.getAllUsersPaginated(page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * GET ALL USERS WITH FILTERS AND PAGINATION
     *
     * Filters:
     * - sambhagId: Filter by Sambhag (Division) ID
     * - districtId: Filter by District ID
     * - blockId: Filter by Block ID
     * - name: Search by name or surname (partial match)
     * - mobile: Search by mobile number (partial match)
     *
     * Usage: GET /api/users/filter?sambhagId=1&districtId=2&blockId=3&name=राहुल&mobile=98765&page=0&size=20
     */
    @GetMapping("/filter")
    public ResponseEntity<PageResponse<UserResponse>> getAllUsersFiltered(
            @RequestParam(required = false) String sambhagId,
            @RequestParam(required = false) String districtId,
            @RequestParam(required = false) String blockId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String mobile,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "250") int size) {
        PageResponse<UserResponse> response = userService.getAllUsersFiltered(
                sambhagId, districtId, blockId, name, mobile, page, size);
        return ResponseEntity.ok(response);
    }

    // UPDATE USER
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String id,
            @RequestBody UpdateUserRequest req) {
        return ResponseEntity.ok(userService.updateUser(id, req));
    }

    /**
     * UPDATE PASSWORD
     * Requires: currentPassword, newPassword, confirmPassword
     *
     * Usage: PUT /api/users/{id}/password
     *
     * Response: { "success": true, "message": "Password changed successfully" }
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(
            @PathVariable String id,
            @Valid @RequestBody UpdatePasswordRequest req) {
        try {
            userService.updatePassword(
                id,
                req.getCurrentPassword(),
                req.getNewPassword()
            );
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password changed successfully"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to update password: " + e.getMessage()
            ));
        }
    }
}

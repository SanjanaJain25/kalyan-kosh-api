package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.RegisterRequest;
import com.example.kalyan_kosh_api.dto.UpdateUserRequest;
import com.example.kalyan_kosh_api.dto.UserResponse;
import com.example.kalyan_kosh_api.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }



    // GET USER BY ID
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // GET ALL USERS
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // UPDATE USER
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String id,
            @RequestBody UpdateUserRequest req) {
        return ResponseEntity.ok(userService.updateUser(id, req));
    }
}

package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.LoginRequest;
import com.example.kalyan_kosh_api.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestBody LoginRequest req) {

        String token = authService.authenticateAndGetToken(
                req.getUsername(),
                req.getPassword()
        );

        return ResponseEntity.ok(
                Map.of(
                        "token", token,
                        "type", "Bearer"
                )
        );
    }
}










































//package com.example.kalyan_kosh_api.controller;
//
//import com.example.kalyan_kosh_api.dto.LoginRequest;
//import com.example.kalyan_kosh_api.dto.RegisterRequest;
//import com.example.kalyan_kosh_api.dto.UserResponse;
//import com.example.kalyan_kosh_api.entity.User;
//import com.example.kalyan_kosh_api.service.AuthService;
//import org.modelmapper.ModelMapper;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/auth")
//public class AuthController {
//
//    private final AuthService authService;
//    private final ModelMapper mapper;
//
//    public AuthController(AuthService authService, ModelMapper mapper) {
//        this.authService = authService;
//        this.mapper = mapper;
//    }
//
//    @PostMapping("/register")
//    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest req) {
//        User user = authService.register(req);
//        UserResponse resp = mapper.map(user, UserResponse.class);
//        return ResponseEntity.ok(resp);
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest req) {
//        String token = authService.authenticateAndGetToken(req.getUsername(), req.getPassword());
//        // return token in a JSON object { "token": "..." }
//        return ResponseEntity.ok(Map.of("token", token));
//    }
//}

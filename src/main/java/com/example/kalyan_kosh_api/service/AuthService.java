package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.RegisterRequest;
import com.example.kalyan_kosh_api.dto.LoginResponse;
import com.example.kalyan_kosh_api.dto.UserResponse;
import com.example.kalyan_kosh_api.entity.*;
import com.example.kalyan_kosh_api.repository.*;
import com.example.kalyan_kosh_api.security.CustomUserDetailsService;
import com.example.kalyan_kosh_api.security.JwtUtil;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final ModelMapper mapper;
    private final IdGeneratorService idGeneratorService;

    // Constructor with required dependencies (Spring will autowire)
    public AuthService(UserRepository userRepo,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       CustomUserDetailsService userDetailsService,
                       JwtUtil jwtUtil,
                       ModelMapper mapper,
                       IdGeneratorService idGeneratorService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.mapper = mapper;
        this.idGeneratorService = idGeneratorService;
    }

    @Transactional
    public User registerAfterOtp(RegisterRequest req) {

        if (userRepo.existsById(req.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        User u = new User();
        u.setName(req.getName());
        u.setSurname(req.getSurname());
        u.setUsername(req.getUsername());
        u.setEmail(req.getEmail());
        u.setMobileNumber(req.getMobileNumber());
        u.setPhoneNumber(req.getPhoneNumber());
        u.setCountryCode(req.getCountryCode());
        u.setGender(req.getGender());
        u.setMaritalStatus(req.getMaritalStatus());
        u.setHomeAddress(req.getHomeAddress());
        u.setSchoolOfficeName(req.getSchoolOfficeName());
        u.setDepartment(req.getDepartment());
        u.setDepartmentUniqueId(req.getDepartmentUniqueId());
        // Note: departmentDistrict and departmentBlock removed due to entity type mismatch
        u.setNominee1Name(req.getNominee1Name());
        u.setNominee1Relation(req.getNominee1Relation());
        u.setNominee2Name(req.getNominee2Name());
        u.setNominee2Relation(req.getNominee2Relation());
        u.setAcceptedTerms(req.isAcceptedTerms());

        if (req.getDateOfBirth() != null && !req.getDateOfBirth().isEmpty()) {
            try {
                u.setDateOfBirth(LocalDate.parse(req.getDateOfBirth()));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format. Use yyyy-MM-dd");
            }
        }

        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        u.setRole(Role.ROLE_USER);

        // Generate custom ID (PMUMS2024XXXXX format)
        String userId = idGeneratorService.generateNextUserId();
        u.setId(userId);

        return userRepo.save(u);
    }

    public String authenticateAndGetToken(
            String username,
            String rawPassword) {

        // Authenticate with username/password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        rawPassword
                )
        );

        // Find user by username to get userId
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        // Load UserDetails using userId (for JWT generation)
        UserDetails ud = userDetailsService.loadUserByUsername(user.getId());

        return jwtUtil.generateToken(ud);
    }

    /**
     * Authenticate credentials and return login response with both JWT token and user details.
     * Throws AuthenticationException (runtime) if credentials invalid.
     */
    public LoginResponse authenticateAndGetLoginResponse(String username, String rawPassword) {
        // this will throw a subclass of AuthenticationException if auth fails
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, rawPassword));

        // Find user by username to get the user entity and userId
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        // Load UserDetails using userId (this is what goes into JWT)
        UserDetails ud = userDetailsService.loadUserByUsername(user.getId());

        // Generate JWT using JwtUtil (will contain userId as subject)
        String token = jwtUtil.generateToken(ud);

        // Map user entity to response DTO
        UserResponse userResponse = mapper.map(user, UserResponse.class);

        return new LoginResponse(token, userResponse);
    }

    @Transactional
    public void resetPassword(String mobile, String newPassword) {

        User user = userRepo.findByMobileNumber(mobile)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String encoded =
                passwordEncoder.encode(newPassword);

        user.setPasswordHash(encoded);

        userRepo.save(user);
    }

}


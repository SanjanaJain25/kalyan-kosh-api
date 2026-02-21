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

import java.time.Instant;
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
    private final StateRepository stateRepo;
    private final SambhagRepository sambhagRepo;
    private final DistrictRepository districtRepo;
    private final BlockRepository blockRepo;
    private final EmailService emailService;

    // Constructor with required dependencies (Spring will autowire)
    public AuthService(UserRepository userRepo,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       CustomUserDetailsService userDetailsService,
                       JwtUtil jwtUtil,
                       ModelMapper mapper,
                       IdGeneratorService idGeneratorService,
                       StateRepository stateRepo,
                       SambhagRepository sambhagRepo,
                       DistrictRepository districtRepo,
                       BlockRepository blockRepo,
                       EmailService emailService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.mapper = mapper;
        this.idGeneratorService = idGeneratorService;
        this.stateRepo = stateRepo;
        this.sambhagRepo = sambhagRepo;
        this.districtRepo = districtRepo;
        this.blockRepo = blockRepo;
        this.emailService = emailService;
    }

    @Transactional
    public User registerAfterOtp(RegisterRequest req) {
        // Check if user with this email already exists
        if (userRepo.findByEmail(req.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        User u = new User();

        try {
            // Set basic fields
            u.setName(req.getName());
            u.setSurname(req.getSurname());
            u.setFatherName(req.getFatherName());
            u.setEmail(req.getEmail());
            u.setMobileNumber(req.getMobileNumber());
            u.setCountryCode(req.getCountryCode());
            u.setPincode(req.getPincode());
            u.setGender(req.getGender());
            u.setMaritalStatus(req.getMaritalStatus());
            u.setHomeAddress(req.getHomeAddress());

            // Set school/office fields
            u.setSchoolOfficeName(req.getSchoolOfficeName());
            u.setSankulName(req.getSankulName());
            u.setDepartment(req.getDepartment());

            // Set department unique ID only if provided
            if (req.getDepartmentUniqueId() != null && !req.getDepartmentUniqueId().trim().isEmpty()) {
                u.setDepartmentUniqueId(req.getDepartmentUniqueId().trim());
            } else {
                u.setDepartmentUniqueId(null);  // Allow NULL for users without employee ID
            }

            // Set location entities (State, Sambhag, District, Block)
            if (req.getDepartmentState() != null && !req.getDepartmentState().isEmpty()) {
                State state = stateRepo.findByName(req.getDepartmentState())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid state: " + req.getDepartmentState()));
                u.setDepartmentState(state);

                if (req.getDepartmentSambhag() != null && !req.getDepartmentSambhag().isEmpty()) {
                    Sambhag sambhag = sambhagRepo.findByNameAndState(req.getDepartmentSambhag(), state)
                            .orElseThrow(() -> new IllegalArgumentException("Invalid sambhag: " + req.getDepartmentSambhag()));
                    u.setDepartmentSambhag(sambhag);

                    if (req.getDepartmentDistrict() != null && !req.getDepartmentDistrict().isEmpty()) {
                        District district = districtRepo.findByNameAndSambhag(req.getDepartmentDistrict(), sambhag)
                                .orElseThrow(() -> new IllegalArgumentException("Invalid district: " + req.getDepartmentDistrict()));
                        u.setDepartmentDistrict(district);

                        if (req.getDepartmentBlock() != null && !req.getDepartmentBlock().isEmpty()) {
                            Block block = blockRepo.findByNameAndDistrict(req.getDepartmentBlock(), district)
                                    .orElseThrow(() -> new IllegalArgumentException("Invalid block: " + req.getDepartmentBlock()));
                            u.setDepartmentBlock(block);
                        }
                    }
                }
            }

            // Set nominee information
            u.setNominee1Name(req.getNominee1Name());
            u.setNominee1Relation(req.getNominee1Relation());
            u.setNominee2Name(req.getNominee2Name());
            u.setNominee2Relation(req.getNominee2Relation());

            // Parse dates
            if (req.getDateOfBirth() != null && !req.getDateOfBirth().isEmpty()) {
                try {
                    u.setDateOfBirth(LocalDate.parse(req.getDateOfBirth()));
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Invalid date format. Use yyyy-MM-dd");
                }
            }

            if (req.getJoiningDate() != null && !req.getJoiningDate().isEmpty()) {
                try {
                    u.setJoiningDate(LocalDate.parse(req.getJoiningDate()));
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Invalid joining date format. Use yyyy-MM-dd");
                }
            }

            if (req.getRetirementDate() != null && !req.getRetirementDate().isEmpty()) {
                try {
                    u.setRetirementDate(LocalDate.parse(req.getRetirementDate()));
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Invalid retirement date format. Use yyyy-MM-dd");
                }
            }

            // Encode password and set role
            u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
            u.setRole(Role.ROLE_USER);

            // Generate user ID
            String userId = idGeneratorService.generateNextUserId();
            u.setId(userId);

            // Set timestamps manually (createdAt should only be set once during creation)
            u.setCreatedAt(Instant.now());
            u.setUpdatedAt(Instant.now());

            // Save user
            User savedUser = userRepo.save(u);

            // Send registration confirmation email
            // Only use name field for greeting (surname is separate)
            String fullName = savedUser.getName();
            if (savedUser.getSurname() != null && !savedUser.getSurname().trim().isEmpty()) {
                // Only append surname if it's not already part of the name
                if (!fullName.toLowerCase().contains(savedUser.getSurname().toLowerCase().trim())) {
                    fullName = fullName + " " + savedUser.getSurname().trim();
                }
            }
            emailService.sendRegistrationConfirmationEmail(savedUser.getEmail(), fullName.trim(), savedUser.getId());

            return savedUser;

        } catch (Exception e) {
            throw e;
        }
    }

    public String authenticateAndGetToken(
            String email,
            String rawPassword) {

        // Authenticate with email/password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        rawPassword
                )
        );

        // Find user by email to get userId
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        // Load UserDetails using userId (for JWT generation)
        UserDetails ud = userDetailsService.loadUserByUsername(user.getId());

        return jwtUtil.generateToken(ud);
    }

    /**
     * Authenticate credentials and return login response with both JWT token and user details.
     * Throws AuthenticationException (runtime) if credentials invalid.
     */
    public LoginResponse authenticateAndGetLoginResponse(String userId, String rawPassword) {
        // this will throw a subclass of AuthenticationException if auth fails
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userId, rawPassword));

        // Find user by userId to get the user entity
        User user = userRepo.findById(userId)
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
        user.setUpdatedAt(Instant.now());

        userRepo.save(user);
    }

    @Transactional
    public void resetPasswordByEmail(String email, String newPassword) {

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email"));

        String encoded = passwordEncoder.encode(newPassword);

        user.setPasswordHash(encoded);
        user.setUpdatedAt(Instant.now());

        userRepo.save(user);
    }
}


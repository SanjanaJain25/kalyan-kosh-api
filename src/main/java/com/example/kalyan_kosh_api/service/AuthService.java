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
    private final BlockRepository blockRepo;
    private final DistrictRepository districtRepo;
    private final SambhagRepository sambhagRepo;
    private final StateRepository stateRepo;

    // Constructor with required dependencies (Spring will autowire)
    public AuthService(UserRepository userRepo,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       CustomUserDetailsService userDetailsService,
                       JwtUtil jwtUtil,
                       ModelMapper mapper,
                       IdGeneratorService idGeneratorService,
                       BlockRepository blockRepo,
                       DistrictRepository districtRepo,
                       SambhagRepository sambhagRepo,
                       StateRepository stateRepo) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.mapper = mapper;
        this.idGeneratorService = idGeneratorService;
        this.blockRepo = blockRepo;
        this.districtRepo = districtRepo;
        this.sambhagRepo = sambhagRepo;
        this.stateRepo = stateRepo;
    }

    @Transactional
    public User registerAfterOtp(RegisterRequest req) {

        if (userRepo.existsByUsername(req.getUsername())) {
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

        // Validate and set location hierarchy: State → Sambhag → District → Block
        State state = null;
        Sambhag sambhag = null;
        District district = null;

        // 1. Validate and set State
        if (req.getDepartmentState() != null && !req.getDepartmentState().isEmpty()) {
            state = stateRepo.findByName(req.getDepartmentState())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Invalid state: " + req.getDepartmentState()));
            u.setDepartmentState(state);
        }

        // 2. Validate and set Sambhag (must belong to state)
        if (req.getDepartmentSambhag() != null && !req.getDepartmentSambhag().isEmpty()) {
            if (state == null) {
                throw new IllegalArgumentException("State must be provided when specifying sambhag");
            }
            sambhag = sambhagRepo.findByNameAndState(req.getDepartmentSambhag(), state)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Invalid sambhag: " + req.getDepartmentSambhag() +
                            " for state: " + req.getDepartmentState()));
            u.setDepartmentSambhag(sambhag);
        }

        // 3. Validate and set District (must belong to sambhag)
        if (req.getDepartmentDistrict() != null && !req.getDepartmentDistrict().isEmpty()) {
            if (sambhag == null) {
                // Try to find district by name only (backward compatibility)
                district = districtRepo.findByName(req.getDepartmentDistrict())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Invalid district: " + req.getDepartmentDistrict()));
            } else {
                // Validate district belongs to sambhag
                district = districtRepo.findByNameAndSambhag(req.getDepartmentDistrict(), sambhag)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Invalid district: " + req.getDepartmentDistrict() +
                                " for sambhag: " + req.getDepartmentSambhag()));
            }
            u.setDepartmentDistrict(district);
        }

        // 4. Validate and set Block (must belong to district)
        if (req.getDepartmentBlock() != null && !req.getDepartmentBlock().isEmpty()) {
            if (district == null) {
                throw new IllegalArgumentException("District must be provided when specifying block");
            }
            Block block = blockRepo.findByNameAndDistrict(req.getDepartmentBlock(), district)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Invalid block: " + req.getDepartmentBlock() +
                            " for district: " + req.getDepartmentDistrict()));
            u.setDepartmentBlock(block);
        }

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

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        rawPassword
                )
        );

        UserDetails ud =
                userDetailsService.loadUserByUsername(username);

        return jwtUtil.generateToken(ud);
    }

    /**
     * Authenticate credentials and return login response with both JWT token and user details.
     * Throws AuthenticationException (runtime) if credentials invalid.
     */
    public LoginResponse authenticateAndGetLoginResponse(String username, String rawPassword) {
        // this will throw a subclass of AuthenticationException if auth fails
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, rawPassword));

        // Load UserDetails (so roles/authorities are available)
        UserDetails ud = userDetailsService.loadUserByUsername(username);

        // Generate JWT using JwtUtil
        String token = jwtUtil.generateToken(ud);

        // Get user entity and map to response DTO
        User user = userRepo.findById(username)
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));
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

//package com.example.kalyan_kosh_api.service;
//
//import com.example.kalyan_kosh_api.dto.RegisterRequest;
//import com.example.kalyan_kosh_api.entity.Role;
//import com.example.kalyan_kosh_api.entity.User;
//import com.example.kalyan_kosh_api.repository.UserRepository;
//import com.example.kalyan_kosh_api.security.CustomUserDetailsService;
//import com.example.kalyan_kosh_api.security.JwtUtil;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.time.format.DateTimeParseException;
//
//@Service
//public class AuthService {
//
//    private final UserRepository userRepo;
//    private final PasswordEncoder passwordEncoder;
//    private final AuthenticationManager authenticationManager;
//    private final CustomUserDetailsService userDetailsService;
//    private final JwtUtil jwtUtil;
//
//    // Constructor with required dependencies (Spring will autowire)
//    public AuthService(UserRepository userRepo,
//                       PasswordEncoder passwordEncoder,
//                       AuthenticationManager authenticationManager,
//                       CustomUserDetailsService userDetailsService,
//                       JwtUtil jwtUtil) {
//        this.userRepo = userRepo;
//        this.passwordEncoder = passwordEncoder;
//        this.authenticationManager = authenticationManager;
//        this.userDetailsService = userDetailsService;
//        this.jwtUtil = jwtUtil;
//    }
//
//    @Transactional
//    public User register(RegisterRequest req) {
//
//        // basic validation
//        if (req.getUsername() == null || req.getUsername().isBlank()) {
//            throw new IllegalArgumentException("Username is required");
//        }
//        if (req.getPassword() == null || req.getPassword().length() < 6) {
//            throw new IllegalArgumentException("Password must be at least 6 characters");
//        }
//        if (userRepo.existsByUsername(req.getUsername())) {
//            throw new IllegalArgumentException("Username already exists");
//        }
//
//        // map DTO -> entity
//        User u = new User();
//        u.setName(req.getName());
//        u.setSurname(req.getSurname());
//        u.setCountryCode(req.getCountryCode());
//        u.setPhoneNumber(req.getPhoneNumber());
//        u.setMobileNumber(req.getMobileNumber());
//        u.setEmail(req.getEmail());
//        u.setGender(req.getGender());
//        u.setMaritalStatus(req.getMaritalStatus());
//        u.setUsername(req.getUsername());
//        u.setHomeAddress(req.getHomeAddress());
//        u.setSchoolOfficeName(req.getSchoolOfficeName());
//        u.setDepartment(req.getDepartment());
//        u.setDepartmentUniqueId(req.getDepartmentUniqueId());
//        u.setDepartmentDistrict(req.getDepartmentDistrict());
//        u.setDepartmentBlock(req.getDepartmentBlock());
//        u.setNominee1Name(req.getNominee1Name());
//        u.setNominee1Relation(req.getNominee1Relation());
//        u.setNominee2Name(req.getNominee2Name());
//        u.setNominee2Relation(req.getNominee2Relation());
//        u.setAcceptedTerms(req.isAcceptedTerms());
//
//        // parse dateOfBirth if provided
//        if (req.getDateOfBirth() != null && !req.getDateOfBirth().isBlank()) {
//            try {
//                LocalDate dob = LocalDate.parse(req.getDateOfBirth()); // expects yyyy-MM-dd
//                u.setDateOfBirth(dob);
//            } catch (DateTimeParseException ex) {
//                throw new IllegalArgumentException("dateOfBirth must be in yyyy-MM-dd format");
//            }
//        }
//
//        // hash password
//        String hashed = passwordEncoder.encode(req.getPassword());
//        u.setPasswordHash(hashed);
//
//        // default role (entity already had ROLE_USER default but set explicitly)
//        u.setRole(Role.ROLE_USER);
//
//        return userRepo.save(u);
//    }
//
//    /**
//     * Authenticate credentials and return JWT token.
//     * Throws AuthenticationException (runtime) if credentials invalid.
//     */
//    public String authenticateAndGetToken(String username, String rawPassword) {
//        // this will throw a subclass of AuthenticationException if auth fails
//        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, rawPassword));
//
//        // Load UserDetails (so roles/authorities are available)
//        UserDetails ud = userDetailsService.loadUserByUsername(username);
//
//        // Generate JWT using JwtUtil
//        return jwtUtil.generateToken(ud);
//    }
//}

package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.UpdateUserRequest;
import com.example.kalyan_kosh_api.dto.UserResponse;
import com.example.kalyan_kosh_api.entity.Role;
import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setId("PMUMS202458108");
        testUser.setUsername("testuser");
        testUser.setName("Test");
        testUser.setSurname("User");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.ROLE_USER);
        testUser.setPhoneNumber("1234567890");
        testUser.setDepartment("IT");
        testUser.setDepartmentUniqueId("DEPT001");
    }

    @Test
    public void testGetUserById_Success() {
        when(userRepository.findById("PMUMS202458108")).thenReturn(Optional.of(testUser));

        UserResponse result = userService.getUserById("PMUMS202458108");

        assertNotNull(result);
        assertEquals("PMUMS202458108", result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("Test", result.getName());
        assertEquals("User", result.getSurname());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    public void testGetUserById_NotFound() {
        when(userRepository.findById("PMUMS202499999")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userService.getUserById("PMUMS202499999"));
    }

    @Test
    public void testGetAllUsers() {
        User user2 = new User();
        user2.setId("PMUMS202458109");
        user2.setUsername("user2");
        user2.setName("Second");

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        List<UserResponse> results = userService.getAllUsers();

        assertEquals(2, results.size());
        assertEquals("testuser", results.get(0).getUsername());
        assertEquals("user2", results.get(1).getUsername());
    }

    @Test
    public void testUpdateUser_Success() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated");
        request.setEmail("updated@example.com");
        request.setDepartmentUniqueId("NEWDEPT");

        when(userRepository.findById("PMUMS202458108")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse result = userService.updateUser("PMUMS202458108", request);

        assertNotNull(result);
        verify(userRepository).save(testUser);
        // Verify that fields were updated
        assertEquals("Updated", testUser.getName());
        assertEquals("updated@example.com", testUser.getEmail());
        assertEquals("NEWDEPT", testUser.getDepartmentUniqueId());
        // Surname should remain unchanged
        assertEquals("User", testUser.getSurname());
    }

    @Test
    public void testUpdateUser_PartialUpdate() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("newemail@example.com");
        // Only update email, other fields null

        when(userRepository.findById("PMUMS202458108")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse result = userService.updateUser("PMUMS202458108", request);

        assertNotNull(result);
        assertEquals("newemail@example.com", testUser.getEmail());
        // Other fields should remain unchanged
        assertEquals("Test", testUser.getName());
        assertEquals("User", testUser.getSurname());
    }

    @Test
    public void testUpdateUser_NotFound() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated");

        when(userRepository.findById("PMUMS202499999")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userService.updateUser("PMUMS202499999", request));
    }
}


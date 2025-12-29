package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.LoginRequest;
import com.example.kalyan_kosh_api.dto.LoginResponse;
import com.example.kalyan_kosh_api.dto.RegisterRequest;
import com.example.kalyan_kosh_api.dto.UserResponse;
import com.example.kalyan_kosh_api.entity.Role;
import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for unit tests
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private ModelMapper modelMapper;

    @Test
    public void testRegister() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setName("Test");
        request.setSurname("User");
        request.setEmail("test@example.com");

        User user = new User();
        user.setId("PMUMS202458108");
        user.setUsername("testuser");
        user.setName("Test");
        user.setSurname("User");
        user.setEmail("test@example.com");
        user.setRole(Role.ROLE_USER);

        UserResponse response = new UserResponse();
        response.setId("PMUMS202458108");
        response.setUsername("testuser");
        response.setName("Test");
        response.setSurname("User");
        response.setEmail("test@example.com");
        response.setRole(Role.ROLE_USER);

        when(authService.register(any(RegisterRequest.class))).thenReturn(user);
        when(modelMapper.map(user, UserResponse.class)).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("PMUMS202458108"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.name").value("Test"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    public void testLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        UserResponse userResponse = new UserResponse();
        userResponse.setId("PMUMS202458108");
        userResponse.setUsername("testuser");
        userResponse.setName("Test");
        userResponse.setRole(Role.ROLE_USER);

        LoginResponse loginResponse = new LoginResponse("jwt-token-here", userResponse);

        when(authService.authenticateAndGetLoginResponse(anyString(), anyString())).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-here"))
                .andExpect(jsonPath("$.user.id").value("PMUMS202458108"))
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.user.name").value("Test"));
    }
}


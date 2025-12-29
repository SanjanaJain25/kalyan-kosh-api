package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.UpdateUserRequest;
import com.example.kalyan_kosh_api.dto.UserResponse;
import com.example.kalyan_kosh_api.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for unit tests
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    public void testGetUserById() throws Exception {
        UserResponse user = new UserResponse();
        user.setId("PMUMS202458108");
        user.setName("Test");
        user.setSurname("User");
        user.setEmail("test@example.com");

        when(userService.getUserById("PMUMS202458108")).thenReturn(user);

        mockMvc.perform(get("/api/users/PMUMS202458108"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("PMUMS202458108"))
                .andExpect(jsonPath("$.name").value("Test"))
                .andExpect(jsonPath("$.surname").value("User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    public void testGetAllUsers() throws Exception {
        UserResponse user1 = new UserResponse();
        user1.setId("PMUMS202458108");
        user1.setName("Test1");

        UserResponse user2 = new UserResponse();
        user2.setId("PMUMS202458109");
        user2.setName("Test2");

        List<UserResponse> users = Arrays.asList(user1, user2);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("PMUMS202458108"))
                .andExpect(jsonPath("$[0].name").value("Test1"))
                .andExpect(jsonPath("$[1].id").value("PMUMS202458109"))
                .andExpect(jsonPath("$[1].name").value("Test2"));
    }

    @Test
    public void testUpdateUser() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated");
        request.setSurname("User");
        request.setEmail("updated@example.com");

        UserResponse response = new UserResponse();
        response.setId("PMUMS202458108");
        response.setName("Updated");
        response.setSurname("User");
        response.setEmail("updated@example.com");

        when(userService.updateUser(eq("PMUMS202458108"), any(UpdateUserRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/users/PMUMS202458108")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("PMUMS202458108"))
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.surname").value("User"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }
}


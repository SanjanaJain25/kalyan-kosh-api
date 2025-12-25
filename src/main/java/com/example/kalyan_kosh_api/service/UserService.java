package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.UpdateUserRequest;
import com.example.kalyan_kosh_api.dto.UserResponse;
import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.repository.UserRepository;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final ModelMapper mapper;

    public UserService(UserRepository userRepo, ModelMapper mapper) {
        this.userRepo = userRepo;
        this.mapper = mapper;
    }

    public UserResponse getUserById(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return mapper.map(user, UserResponse.class);
    }

    public List<UserResponse> getAllUsers() {
        return userRepo.findAll()
                .stream()
                .map(u -> mapper.map(u, UserResponse.class))
                .toList();
    }

    public UserResponse updateUser(Long id, UpdateUserRequest req) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Copy only non-null fields
        mapper.map(req, user);

        userRepo.save(user);

        return mapper.map(user, UserResponse.class);
    }
}

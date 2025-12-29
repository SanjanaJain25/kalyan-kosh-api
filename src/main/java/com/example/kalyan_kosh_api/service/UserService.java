package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.UpdateUserRequest;
import com.example.kalyan_kosh_api.dto.UserResponse;
import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public UserResponse getUserById(String id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return toUserResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepo.findAll()
                .stream()
                .map(this::toUserResponse)
                .toList();
    }

    public UserResponse updateUser(String id, UpdateUserRequest req) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Use reflection to copy non-null fields from request to user entity
        copyNonNullFields(req, user);

        userRepo.save(user);

        return toUserResponse(user);
    }

    /**
     * Utility method to copy non-null fields from source to target using reflection
     */
    private void copyNonNullFields(Object source, Object target) {
        Field[] sourceFields = source.getClass().getDeclaredFields();

        for (Field sourceField : sourceFields) {
            try {
                sourceField.setAccessible(true);
                Object value = sourceField.get(source);

                if (value != null) {
                    Field targetField = target.getClass().getDeclaredField(sourceField.getName());
                    targetField.setAccessible(true);
                    targetField.set(target, value);
                }
            } catch (NoSuchFieldException e) {
                // Field doesn't exist in target class, skip it
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error copying field: " + sourceField.getName(), e);
            }
        }
    }

    /**
     * Convert User entity to UserResponse DTO using reflection
     */
    private UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        copyMatchingFields(user, response);
        return response;
    }

    /**
     * Utility method to copy matching fields between objects using reflection
     */
    private void copyMatchingFields(Object source, Object target) {
        Field[] targetFields = target.getClass().getDeclaredFields();

        for (Field targetField : targetFields) {
            try {
                Field sourceField = source.getClass().getDeclaredField(targetField.getName());

                sourceField.setAccessible(true);
                targetField.setAccessible(true);

                Object value = sourceField.get(source);
                targetField.set(target, value);

            } catch (NoSuchFieldException e) {
                // Field doesn't exist in source class, skip it
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error copying field: " + targetField.getName(), e);
            }
        }
    }
}

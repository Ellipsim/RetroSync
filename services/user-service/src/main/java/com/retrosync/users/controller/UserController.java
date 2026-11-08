package com.retrosync.users.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.retrosync.users.dto.LoginRequest;
import com.retrosync.users.dto.RegisterRequest;
import com.retrosync.users.dto.UserResponse;
import com.retrosync.users.model.User;
import com.retrosync.users.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        try {
            User user = userService.register(
                request.email(),
                request.password()
            );

            UserResponse response = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getCreatedAt()
            );

            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);

        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                exception.getMessage()
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(
            @Valid @RequestBody LoginRequest request) {

        try {
            User user = userService.login(
                request.email(),
                request.password()
            );

            UserResponse response = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getCreatedAt()
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Credenciales incorrectas"
            );
        }
    }
}
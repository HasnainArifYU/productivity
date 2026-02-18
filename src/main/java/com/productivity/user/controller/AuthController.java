package com.productivity.user.controller;

import com.productivity.user.dto.JwtResponse;
import com.productivity.user.dto.LoginRequest;
import com.productivity.user.dto.SignupRequest;
import com.productivity.user.dto.UserDto;
import com.productivity.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = userService.authenticateUser(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        UserDto userDto = userService.registerUser(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }
}

package com.productivity.user.service;

import com.productivity.user.dto.JwtResponse;
import com.productivity.user.dto.LoginRequest;
import com.productivity.user.dto.SignupRequest;
import com.productivity.user.dto.UserDto;

import java.util.List;

public interface UserService {
    
    JwtResponse authenticateUser(LoginRequest loginRequest);
    
    UserDto registerUser(SignupRequest signupRequest);
    
    UserDto getCurrentUser();
    
    UserDto getUserById(Long id);
    
    List<UserDto> getAllUsers();
    
    UserDto updateUser(Long id, UserDto userDto);
    
    void deleteUser(Long id);
}

package com.productivity.user.service;

import com.productivity.common.exception.AppException;
import com.productivity.common.exception.BadRequestException;
import com.productivity.common.exception.ResourceNotFoundException;
import com.productivity.security.UserDetailsImpl;
import com.productivity.security.JwtUtils;
import com.productivity.user.dto.JwtResponse;
import com.productivity.user.dto.LoginRequest;
import com.productivity.user.dto.SignupRequest;
import com.productivity.user.dto.UserDto;
import com.productivity.user.entity.ERole;
import com.productivity.user.entity.Role;
import com.productivity.user.entity.User;
import com.productivity.user.repository.RoleRepository;
import com.productivity.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return JwtResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .roles(roles)
                .build();
    }

    @Override
    @Transactional
    public UserDto registerUser(SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new BadRequestException("Email is already in use!");
        }

        // Create new user's account
        User user = User.builder()
                .username(signupRequest.getUsername())
                .email(signupRequest.getEmail())
                .password(encoder.encode(signupRequest.getPassword()))
                .firstName(signupRequest.getFirstName())
                .lastName(signupRequest.getLastName())
                .build();

        Set<String> strRoles = signupRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new AppException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new AppException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new AppException("Error: Role is not found."));
                        roles.add(modRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new AppException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        
        return mapUserToDto(savedUser);
    }

    @Override
    public UserDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        return getUserById(userDetails.getId());
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        return mapUserToDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        
        return users.stream()
                .map(this::mapUserToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        // Check if username already exists for another user
        if (userDto.getUsername() != null && !user.getUsername().equals(userDto.getUsername()) && 
                userRepository.existsByUsername(userDto.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }
        
        // Check if email already exists for another user
        if (userDto.getEmail() != null && !user.getEmail().equals(userDto.getEmail()) && 
                userRepository.existsByEmail(userDto.getEmail())) {
            throw new BadRequestException("Email is already in use!");
        }
        
        // Update fields
        if (userDto.getUsername() != null) {
            user.setUsername(userDto.getUsername());
        }
        
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        
        if (userDto.getFirstName() != null) {
            user.setFirstName(userDto.getFirstName());
        }
        
        if (userDto.getLastName() != null) {
            user.setLastName(userDto.getLastName());
        }
        
        User updatedUser = userRepository.save(user);
        
        return mapUserToDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        userRepository.delete(user);
    }
    
    private UserDto mapUserToDto(User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());
        
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .roles(roles)
                .build();
    }
}

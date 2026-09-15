package com.airline.service.impl;

import com.airline.enums.UserRole;
import com.airline.exception.BusinessException;
import com.airline.exception.EmailAlreadyExistsException;
import com.airline.exception.ErrorCode;
import com.airline.exception.UserNotFoundException;
import com.airline.mapper.UserMapper;
import com.airline.model.User;
import com.airline.payload.request.LoginRequest;
import com.airline.payload.request.UserRequest;
import com.airline.payload.response.AuthResponse;
import com.airline.repository.UserRepository;
import com.airline.security.blacklist.TokenBlacklistService;
import com.airline.security.jwt.JwtService;
import com.airline.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    @Transactional
    public AuthResponse signUp(UserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new EmailAlreadyExistsException(userRequest.getEmail());
        }

        User newUser = userMapper.toEntity(userRequest);
        newUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        newUser.setRole(UserRole.ROLE_USER);

        User savedUser = userRepository.save(newUser);

        String jwt = jwtService.generateToken(savedUser);

        return AuthResponse.builder()
                .jwtToken(jwt)
                .message("Registered successfully")
                .title("Welcome " + savedUser.getFullName() + "!")
                .user(userMapper.toResponse(savedUser))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException(loginRequest.getEmail()));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BusinessException(ErrorCode.USER_INACTIVE, "User account is inactive");
        }

        user.setLastLogin(LocalDateTime.now());

        String jwt = jwtService.generateToken(user);

        return AuthResponse.builder()
                .jwtToken(jwt)
                .message("Login successful")
                .title("Welcome back " + user.getFullName() + "!")
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    public void logout(String token) {
        Instant expiresAt = jwtService.extractExpiration(token);
        tokenBlacklistService.blacklist(token, expiresAt);
    }

}

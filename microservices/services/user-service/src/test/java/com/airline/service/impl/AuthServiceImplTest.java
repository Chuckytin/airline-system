package com.airline.service.impl;

import com.airline.enums.UserRole;
import com.airline.exception.EmailAlreadyExistsException;
import com.airline.exception.UserNotFoundException;
import com.airline.mapper.UserMapper;
import com.airline.model.User;
import com.airline.payload.request.LoginRequest;
import com.airline.payload.request.UserRequest;
import com.airline.payload.response.AuthResponse;
import com.airline.payload.response.UserResponse;
import com.airline.repository.UserRepository;
import com.airline.security.blacklist.TokenBlacklistService;
import com.airline.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserRequest signUpRequest;
    private LoginRequest loginRequest;
    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        signUpRequest = UserRequest.builder()
                .email("john@example.com")
                .password("password123")
                .fullName("John Doe")
                .phoneNumber("+34600000000")
                .build();

        loginRequest = LoginRequest.builder()
                .email("john@example.com")
                .password("password123")
                .build();

        user = User.builder()
                .id(1L)
                .email("john@example.com")
                .password("encodedPassword")
                .fullName("John Doe")
                .phoneNumber("+34600000000")
                .role(UserRole.ROLE_USER)
                .active(true)
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .email("john@example.com")
                .fullName("John Doe")
                .role(UserRole.ROLE_USER)
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("signUp")
    class SignUpTests {

        @Test
        @DisplayName("Should register user successfully")
        void shouldRegisterUserSuccessfully() {
            when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
            when(userMapper.toEntity(signUpRequest)).thenReturn(user);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(userRepository.save(user)).thenReturn(user);
            when(jwtService.generateToken(user)).thenReturn("jwt-token");
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            AuthResponse result = authService.signUp(signUpRequest);

            assertThat(result).isNotNull();
            assertThat(result.getJwtToken()).isEqualTo("jwt-token");
            assertThat(result.getUser().getEmail()).isEqualTo("john@example.com");

            verify(userRepository).save(user);
            verify(jwtService).generateToken(user);
        }

        @Test
        @DisplayName("Should throw when email already exists")
        void shouldThrowWhenEmailExists() {
            when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.signUp(signUpRequest))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessageContaining("User already exists with email: john@example.com");

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("login")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully")
        void shouldLoginSuccessfully() {
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    user.getEmail(), null, null);

            when(authenticationManager.authenticate(any())).thenReturn(auth);
            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
            when(jwtService.generateToken(user)).thenReturn("jwt-token");
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            AuthResponse result = authService.login(loginRequest);

            assertThat(result).isNotNull();
            assertThat(result.getJwtToken()).isEqualTo("jwt-token");
            assertThat(result.getUser().getEmail()).isEqualTo("john@example.com");

            verify(authenticationManager).authenticate(any());
            verify(jwtService).generateToken(user);
        }

        @Test
        @DisplayName("Should throw when credentials are invalid")
        void shouldThrowWhenInvalidCredentials() {
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Invalid credentials");

            verify(userRepository, never()).findByEmail(anyString());
        }

        @Test
        @DisplayName("Should throw when user not found after authentication")
        void shouldThrowWhenUserNotFound() {
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    user.getEmail(), null, null);

            when(authenticationManager.authenticate(any())).thenReturn(auth);
            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(UserNotFoundException.class);

            verify(jwtService, never()).generateToken(any());
        }
    }

    @Nested
    @DisplayName("logout")
    class LogoutTests {

        @Test
        @DisplayName("Should blacklist token successfully")
        void shouldBlacklistToken() {
            String token = "jwt-token";
            Instant expiresAt = Instant.now().plusSeconds(3600);

            when(jwtService.extractExpiration(token)).thenReturn(expiresAt);

            authService.logout(token);

            verify(tokenBlacklistService).blacklist(token, expiresAt);
        }
    }
}
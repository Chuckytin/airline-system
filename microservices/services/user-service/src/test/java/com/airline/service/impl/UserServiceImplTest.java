package com.airline.service.impl;

import com.airline.enums.UserRole;
import com.airline.exception.ResourceNotFoundException;
import com.airline.mapper.UserMapper;
import com.airline.model.User;
import com.airline.payload.response.UserResponse;
import com.airline.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
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
                .phoneNumber("+34600000000")
                .role(UserRole.ROLE_USER)
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return user when exists")
        void shouldReturnUserWhenExists() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            UserResponse result = userService.getUserById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getEmail()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("Should throw when user not found")
        void shouldThrowWhenNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found with id: 999");
        }

    }

    @Nested
    @DisplayName("getUserByEmail")
    class GetUserByEmailTests {

        @Test
        @DisplayName("Should return user when exists")
        void shouldReturnUserWhenExists() {
            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            UserResponse result = userService.getUserByEmail("john@example.com");

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("Should throw when email not found")
        void shouldThrowWhenEmailNotFound() {
            when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserByEmail("notfound@example.com"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found with email: notfound@example.com");
        }

    }

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return paginated users")
        void shouldReturnPaginatedUsers() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> page = new PageImpl<>(List.of(user));

            when(userRepository.findAll(pageable)).thenReturn(page);
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            Page<UserResponse> result = userService.getAllUsers(pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst().getEmail()).isEqualTo("john@example.com");
        }

    }

}
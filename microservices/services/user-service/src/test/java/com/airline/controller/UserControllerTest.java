package com.airline.controller;

import com.airline.enums.UserRole;
import com.airline.model.User;
import com.airline.payload.response.UserResponse;
import com.airline.security.filter.JwtAuthFilter;
import com.airline.security.userdetails.UserDetailsImpl;
import com.airline.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userResponse = UserResponse.builder()
                .id(1L)
                .email("john@example.com")
                .fullName("John Doe")
                .role(UserRole.ROLE_USER)
                .active(true)
                .build();

        User user = User.builder()
                .id(1L)
                .email("john@example.com")
                .password("encoded")
                .fullName("John Doe")
                .role(UserRole.ROLE_USER)
                .active(true)
                .build();

        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        // Al tener addFilters = false, ningún filtro de Spring Security se ejecuta,
        // así que metemos la autenticación directamente en el SecurityContextHolder
        // en lugar de depender de .with(user(...)), que solo la guarda en sesión.
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /users/me - Should return current user")
    void shouldGetCurrentUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.fullName").value("John Doe"));
    }

    @Test
    @DisplayName("GET /users/{id} - Should return user by ID")
    void shouldGetUserById() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @DisplayName("GET /users - Should return paginated users")
    void shouldGetAllUsers() throws Exception {
        Page<UserResponse> page = new PageImpl<>(List.of(userResponse));

        when(userService.getAllUsers(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("john@example.com"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

}
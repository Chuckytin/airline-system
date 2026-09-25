package com.airline.controller;

import com.airline.payload.response.AuthResponse;
import com.airline.payload.response.UserResponse;
import com.airline.security.filter.JwtAuthFilter;
import com.airline.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .email("john@example.com")
                .fullName("John Doe")
                .build();

        authResponse = AuthResponse.builder()
                .jwtToken("jwt-token")
                .message("Success")
                .title("Welcome")
                .user(userResponse)
                .build();
    }

    @Test
    @DisplayName("POST /auth/register - Should return 201")
    void shouldRegister() throws Exception {
        when(authService.signUp(any())).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "john@example.com",
                                    "password": "password123",
                                    "fullName": "John Doe"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jwtToken").value("jwt-token"))
                .andExpect(jsonPath("$.user.email").value("john@example.com"));
    }

    @Test
    @DisplayName("POST /auth/login - Should return 200")
    void shouldLogin() throws Exception {
        when(authService.login(any())).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "john@example.com",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtToken").value("jwt-token"));
    }

    @Test
    @DisplayName("POST /auth/logout - Should return 204")
    void shouldLogout() throws Exception {
        doNothing().when(authService).logout(anyString());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer jwt-token"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /auth/register - Should return 400 when validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "invalid-email",
                                    "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

}
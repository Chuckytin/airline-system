package com.airline.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Security Integration Tests")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should return 401 when no JWT provided")
    void shouldReturn401WhenNoJwt() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 when invalid JWT provided")
    void shouldReturn401WhenInvalidJwt() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should allow access to public endpoints without JWT")
    void shouldAllowPublicEndpoints() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "admin@test.com",
                                    "password": "Admin1234!"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 403 when accessing admin-only endpoint with USER role")
    void shouldReturn403ForAdminEndpoint() throws Exception {
        // 1. Registra el usuario normal (ROL: ROLE_USER)
        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "normaluser@example.com",
                                    "password": "password123",
                                    "fullName": "Normal User"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        // 2. Extrae el JWT
        String jwt = extractJwtFromResponse(registerResult.getResponse().getContentAsString());

        // 3. Accede al endpoint ADMIN --> 403
        mockMvc.perform(get("/api/v1/users")   // Requiere ROLE_ADMIN
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow access to admin endpoint with ADMIN role")
    void shouldAllowAdminEndpointWithAdminRole() throws Exception {
        // 1. Login como admin (creado por AdminSeeder)
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "admin@test.com",
                                    "password": "Admin1234!"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        // 2. Extrae el JWT
        String adminJwt = extractJwtFromResponse(loginResult.getResponse().getContentAsString());

        // 3. Accede al endpoint ADMIN --> 200
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isOk());
    }

    private String extractJwtFromResponse(String jsonResponse) {
        String pattern = "\"jwtToken\"\\s*:\\s*\"([^\"]+)\"";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(jsonResponse);
        if (m.find()) {
            return m.group(1);
        }
        throw new RuntimeException("JWT not found in response: " + jsonResponse);
    }

}
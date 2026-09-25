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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Auth Integration Tests")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Full auth flow: register → login → access protected endpoint → logout")
    void fullAuthFlow() throws Exception {
        // 1. REGISTER
        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "newuser@example.com",
                                    "password": "password123",
                                    "fullName": "New User",
                                    "phoneNumber": "+34600000000"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jwtToken").exists())
                .andExpect(jsonPath("$.user.email").value("newuser@example.com"))
                .andReturn();

        // Extrae el JWT de la respuesta (sin ObjectMapper)
        String responseBody = registerResult.getResponse().getContentAsString();
        String jwt = extractJwtFromResponse(responseBody);

        // 2. ACCESS PROTECTED ENDPOINT
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newuser@example.com"));

        // 3. LOGIN
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "newuser@example.com",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtToken").exists())
                .andReturn();

        String newJwt = extractJwtFromResponse(loginResult.getResponse().getContentAsString());

        // 4. LOGOUT
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + newJwt))
                .andExpect(status().isNoContent());

        // 5. VERIFY TOKEN IS BLACKLISTED
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + newJwt))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should fail to register with existing email")
    void shouldFailWithExistingEmail() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "admin@test.com",
                                    "password": "password123",
                                    "fullName": "Duplicate User"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
    }

    /**
     * Helper para extraer el JWT de la respuesta JSON usando regex simple.
     */
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
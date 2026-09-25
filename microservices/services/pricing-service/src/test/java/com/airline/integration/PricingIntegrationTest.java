package com.airline.integration;

import com.airline.enums.CabinClass;
import com.airline.enums.FareRuleType;
import com.airline.enums.FareType;
import com.airline.payload.request.BaggagePolicyRequest;
import com.airline.payload.request.FareRequest;
import com.airline.payload.request.FareRuleRequest;
import com.airline.payload.response.FareResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Pricing Integration Tests")
class PricingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Full flow: Fare → FareRule → BaggagePolicy")
    void fullPricingFlow() throws Exception {
        // Creación de un Fare
        FareRequest fareRequest = FareRequest.builder()
                .flightId(1L)
                .cabinClass(CabinClass.ECONOMY)
                .fareType(FareType.STANDARD)
                .basePrice(new BigDecimal("100.00"))
                .taxes(new BigDecimal("21.00"))
                .currency("EUR")
                .validFrom(LocalDate.now())
                .validUntil(LocalDate.now().plusMonths(6))
                .build();

        MvcResult fareResult = mockMvc.perform(post("/api/v1/fares")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fareRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cabinClass").value("ECONOMY"))
                .andExpect(jsonPath("$.totalPrice").value(121.00))
                .andReturn();

        FareResponse createdFare = objectMapper.readValue(
                fareResult.getResponse().getContentAsString(),
                FareResponse.class
        );

        Long fareId = createdFare.getId();
        assert fareId != null;

        // Creación de un FareRule
        FareRuleRequest ruleRequest = FareRuleRequest.builder()
                .fareId(fareId)
                .ruleType(FareRuleType.CHANGE)
                .description("Cambio permitido con penalización")
                .allowed(true)
                .penaltyAmount(new BigDecimal("50.00"))
                .penaltyCurrency("EUR")
                .conditions("Cambios permitidos hasta 24h antes")
                .build();

        mockMvc.perform(post("/api/v1/fare-rules")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ruleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fareId").value(fareId))
                .andExpect(jsonPath("$.ruleType").value("CHANGE"));

        // Creación de un BaggagePolicy
        BaggagePolicyRequest baggageRequest = BaggagePolicyRequest.builder()
                .fareId(fareId)
                .carryOnPieces(1)
                .carryOnWeightKg(7)
                .checkedPieces(1)
                .checkedWeightKg(23)
                .extraBagPrice(new BigDecimal("30.00"))
                .currency("EUR")
                .build();

        mockMvc.perform(post("/api/v1/baggage-policies")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baggageRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fareId").value(fareId))
                .andExpect(jsonPath("$.cabinClass").value("ECONOMY"))   // ← Del Fare
                .andExpect(jsonPath("$.checkedPieces").value(1));

        // Obtener Fare con reglas y baggage
        mockMvc.perform(get("/api/v1/fares/{id}", fareId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(fareId))
                .andExpect(jsonPath("$.cabinClass").value("ECONOMY"));

        // Obtener reglas del Fare
        mockMvc.perform(get("/api/v1/fare-rules/fare/{fareId}", fareId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].ruleType").value("CHANGE"));

        // Obtener BaggagePolicy del Fare
        mockMvc.perform(get("/api/v1/baggage-policies/fare/{fareId}", fareId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fareId").value(fareId))
                .andExpect(jsonPath("$.checkedPieces").value(1));
    }

    @Test
    @DisplayName("Should fail when creating BaggagePolicy for non-existent Fare")
    void shouldFailWhenFareNotFound() throws Exception {
        BaggagePolicyRequest request = BaggagePolicyRequest.builder()
                .fareId(999L)
                .carryOnPieces(1)
                .carryOnWeightKg(7)
                .checkedPieces(0)
                .checkedWeightKg(0)
                .currency("EUR")
                .build();

        mockMvc.perform(post("/api/v1/baggage-policies")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FARE_NOT_FOUND"));
    }

    @Test
    @DisplayName("Should fail when creating duplicate FareRule")
    void shouldFailWhenDuplicateFareRule() throws Exception {
        // Creación de Fare
        FareRequest fareRequest = FareRequest.builder()
                .flightId(1L)
                .cabinClass(CabinClass.BUSINESS)
                .fareType(FareType.FLEX)
                .basePrice(new BigDecimal("500.00"))
                .taxes(new BigDecimal("100.00"))
                .currency("EUR")
                .validFrom(LocalDate.now())
                .build();

        MvcResult fareResult = mockMvc.perform(post("/api/v1/fares")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fareRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        FareResponse fare = objectMapper.readValue(
                fareResult.getResponse().getContentAsString(),
                FareResponse.class
        );

        // Crear 1º regla
        FareRuleRequest ruleRequest = FareRuleRequest.builder()
                .fareId(fare.getId())
                .ruleType(FareRuleType.REFUND)
                .description("Reembolso permitido")
                .build();

        mockMvc.perform(post("/api/v1/fare-rules")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ruleRequest)))
                .andExpect(status().isCreated());

        // Intentar crear 2ª regla con el mismo tipo
        mockMvc.perform(post("/api/v1/fare-rules")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ruleRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("FARE_RULE_ALREADY_EXISTS"));
    }

}
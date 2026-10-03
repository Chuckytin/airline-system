package com.airline.integration;

import com.airline.enums.CabinClass;
import com.airline.payload.request.CabinRequest;
import com.airline.payload.request.SeatAssignmentRequest;
import com.airline.payload.response.CabinResponse;
import com.airline.payload.response.SeatInstanceResponse;
import com.airline.repository.CabinRepository;
import com.airline.repository.SeatInstanceRepository;
import com.airline.repository.SeatMapRepository;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Seat Service Integration Tests")
class SeatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CabinRepository cabinRepository;

    @Autowired
    private SeatInstanceRepository seatInstanceRepository;

    @Autowired
    private SeatMapRepository seatMapRepository;

    @BeforeEach
    void cleanDb() {
        seatInstanceRepository.deleteAllInBatch();
        seatMapRepository.deleteAllInBatch();
        cabinRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("Full flow: Cabin creation -> auto-generated seats -> assign -> release")
    void fullSeatFlow() throws Exception {

        // 1. Crear cabin
        CabinRequest cabinRequest = CabinRequest.builder()
                .flightInstanceId(100L)
                .cabinClass(CabinClass.ECONOMY)
                .rowStart(1).rowEnd(5)
                .columnLayout("ABC-DEF")
                .basePrice(new BigDecimal("50.00"))
                .build();

        MvcResult cabinResult = mockMvc.perform(post("/api/v1/cabins")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cabinRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalSeats").value(30))
                .andExpect(jsonPath("$.availableSeats").value(30))
                .andReturn();

        CabinResponse cabin = objectMapper.readValue(
                cabinResult.getResponse().getContentAsString(),
                CabinResponse.class);

        Long cabinId = cabin.getId();
        assertThat(cabinId).isNotNull();

        // 2. Listar asientos -> deben ser 30
        MvcResult seatsResult = mockMvc.perform(
                        get("/api/v1/seat-instances").param("cabinId", cabinId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(30))
                .andReturn();

        SeatInstanceResponse[] seatsArray = objectMapper.readValue(
                seatsResult.getResponse().getContentAsString(),
                SeatInstanceResponse[].class);

        List<SeatInstanceResponse> seats = Arrays.asList(seatsArray);

        // 3. Verificar tipos de asiento generados para la fila 1
        SeatInstanceResponse seat1A = findByNumber(seats, "1A");
        SeatInstanceResponse seat1B = findByNumber(seats, "1B");
        SeatInstanceResponse seat1C = findByNumber(seats, "1C");
        SeatInstanceResponse seat1D = findByNumber(seats, "1D");
        SeatInstanceResponse seat1E = findByNumber(seats, "1E");
        SeatInstanceResponse seat1F = findByNumber(seats, "1F");

        assertThat(seat1A.getSeatType().name()).isEqualTo("WINDOW");
        assertThat(seat1B.getSeatType().name()).isEqualTo("MIDDLE");
        assertThat(seat1C.getSeatType().name()).isEqualTo("AISLE");
        assertThat(seat1D.getSeatType().name()).isEqualTo("AISLE");
        assertThat(seat1E.getSeatType().name()).isEqualTo("MIDDLE");
        assertThat(seat1F.getSeatType().name()).isEqualTo("WINDOW");

        // 4. Asignar 1A a un booking
        SeatAssignmentRequest assignRequest = SeatAssignmentRequest.builder()
                .bookingId(1L)
                .passengerId(2L)
                .build();

        mockMvc.perform(patch("/api/v1/seat-instances/{id}/assign", seat1A.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OCCUPIED"))
                .andExpect(jsonPath("$.bookingId").value(1L))
                .andExpect(jsonPath("$.passengerId").value(2L));

        // 5. Availability -> availableSeats=29, occupiedSeats=1
        mockMvc.perform(get("/api/v1/cabins/{id}/availability", cabinId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSeats").value(29))
                .andExpect(jsonPath("$.occupiedSeats").value(1))
                .andExpect(jsonPath("$.totalSeats").value(30));

        // 6. Release -> vuelve a AVAILABLE
        mockMvc.perform(patch("/api/v1/seat-instances/{id}/release", seat1A.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.bookingId").doesNotExist())
                .andExpect(jsonPath("$.passengerId").doesNotExist());

        // 7. Availability final
        mockMvc.perform(get("/api/v1/cabins/{id}/availability", cabinId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSeats").value(30))
                .andExpect(jsonPath("$.occupiedSeats").value(0));
    }

    private SeatInstanceResponse findByNumber(List<SeatInstanceResponse> seats, String number) {
        return seats.stream()
                .filter(s -> number.equals(s.getSeatNumber()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Seat " + number + " not found"));
    }
}
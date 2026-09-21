package com.airline.service;

import com.airline.enums.AirlineStatus;
import com.airline.payload.request.AirlineRequest;
import com.airline.payload.response.AirlineDropdownItem;
import com.airline.payload.response.AirlineResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AirlineService {

    AirlineResponse createAirline(AirlineRequest airlineRequest, Long ownerId);

    AirlineResponse getAirlineById(Long id);

    AirlineResponse getAirlineByOwnerId(Long ownerId);

    Page<AirlineResponse> getAllAirlines(Pageable pageable);

    List<AirlineDropdownItem> getAirlinesForDropdown();

    AirlineResponse updateAirline(Long airlineId, AirlineRequest request, Long requesterId);

    AirlineResponse changeStatusByAdmin(Long airlineId, AirlineStatus status, Long requesterId);

    void deleteAirline(Long id, Long ownerId);

}

package com.airline.service;

import com.airline.payload.request.CityRequest;
import com.airline.payload.response.CityResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CityService {

    CityResponse createCity(CityRequest cityRequest);

    CityResponse getCityById(Long id);

    CityResponse updateCity(Long id, CityRequest cityRequest);

    void deleteCityById(Long id);

    Page<CityResponse> getAllCities(Pageable pageable);

    Page<CityResponse> searchCities(String keyword, Pageable pageable);

    Page<CityResponse> getCitiesByCountryCode(String countryCode, Pageable pageable);

    boolean cityExists(String cityCode);

}

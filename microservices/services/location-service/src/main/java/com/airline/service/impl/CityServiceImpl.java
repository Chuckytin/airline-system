package com.airline.service.impl;

import com.airline.mapper.CityMapper;
import com.airline.model.City;
import com.airline.payload.request.CityRequest;
import com.airline.payload.response.CityResponse;
import com.airline.repository.CityRepository;
import com.airline.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;
    private final CityMapper cityMapper;

    @Override
    @Transactional
    public CityResponse createCity(CityRequest cityRequest) {

        if (cityRepository.existsByCityCode(cityRequest.getCityCode())) {
            throw new RuntimeException("City with given code already exists");
        }

        City city = cityMapper.toEntity(cityRequest);
        City saved = cityRepository.save(city);
        return cityMapper.toResponse(saved);
    }

    @Override
    public CityResponse getCityById(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("City not found with given id"));

        return cityMapper.toResponse(city);
    }

    @Override
    @Transactional
    public CityResponse updateCity(Long id, CityRequest cityRequest) {
        City existing = cityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("City not found with given id"));

        if (!cityRequest.getCityCode().equals(existing.getCityCode()) &&
                cityRepository.existsByCityCode(cityRequest.getCityCode())
        ) {
            throw new RuntimeException("City with given code already exists");
        }

        City updated = cityRepository.save(cityMapper.updateEntity(existing, cityRequest));

        return cityMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCityById(Long id) {
        if (!cityRepository.existsById(id)) {
            throw new RuntimeException("City not found with given id");
        }

        cityRepository.deleteById(id);
    }

    @Override
    public Page<CityResponse> getAllCities(Pageable pageable) {
        return cityRepository.findAll(pageable)
                .map(cityMapper::toResponse);
    }

    @Override
    public Page<CityResponse> searchCities(String keyword, Pageable pageable) {
        return cityRepository.searchByKeyword(keyword, pageable)
                .map(cityMapper::toResponse);
    }

    @Override
    public Page<CityResponse> getCitiesByCountryCode(String countryCode, Pageable pageable) {
        return cityRepository.findByCountryCodeIgnoreCase(countryCode, pageable)
                .map(cityMapper::toResponse);
    }

    @Override
    public boolean cityExists(String cityCode) {
        return cityRepository.existsByCityCode(cityCode);
    }

}
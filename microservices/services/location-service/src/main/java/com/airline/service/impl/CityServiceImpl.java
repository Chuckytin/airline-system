package com.airline.service.impl;

import com.airline.exception.ErrorCode;
import com.airline.exception.ResourceAlreadyExistsException;
import com.airline.exception.ResourceNotFoundException;
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
            throw new ResourceAlreadyExistsException(
                    ErrorCode.CITY_CODE_ALREADY_EXISTS,
                    "City", "cityCode", cityRequest.getCityCode()
            );
        }

        City city = cityMapper.toEntity(cityRequest);
        City saved = cityRepository.save(city);
        return cityMapper.toResponse(saved);
    }

    @Override
    public CityResponse getCityById(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.CITY_NOT_FOUND, "City", id
                ));
        return cityMapper.toResponse(city);
    }

    @Override
    @Transactional
    public CityResponse updateCity(Long id, CityRequest cityRequest) {
        City existingCity = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.CITY_NOT_FOUND, "City", id
                ));

        if (!cityRequest.getCityCode().equals(existingCity.getCityCode()) &&
                cityRepository.existsByCityCode(cityRequest.getCityCode())) {
            throw new ResourceAlreadyExistsException(
                    ErrorCode.CITY_CODE_ALREADY_EXISTS,
                    "City", "cityCode", cityRequest.getCityCode()
            );
        }

        cityMapper.updateEntity(existingCity, cityRequest);
        City updatedCity = cityRepository.save(existingCity);
        return cityMapper.toResponse(updatedCity);
    }

    @Override
    @Transactional
    public void deleteCityById(Long id) {
        if (!cityRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCode.CITY_NOT_FOUND, "City", id);
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
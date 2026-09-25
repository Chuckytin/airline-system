package com.airline.service.impl;

import com.airline.config.PricingProperties;
import com.airline.exception.ErrorCode;
import com.airline.exception.ResourceAlreadyExistsException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.exception.ValidationException;
import com.airline.mapper.FareMapper;
import com.airline.model.Fare;
import com.airline.payload.request.FareRequest;
import com.airline.payload.response.FareResponse;
import com.airline.repository.FareRepository;
import com.airline.service.FareService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FareServiceImpl implements FareService {

    private final FareRepository fareRepository;
    private final FareMapper fareMapper;
    private final PricingProperties pricingProperties;

    @Override
    @Transactional
    public FareResponse createFare(FareRequest request, Long requesterId) {

        if (fareRepository.existsByFlightIdAndCabinClassAndFareType(
                request.getFlightId(), request.getCabinClass(), request.getFareType())) {
            throw new ResourceAlreadyExistsException(
                    ErrorCode.FARE_ALREADY_EXISTS,
                    "Fare", "flightId+cabinClass+fareType",
                    request.getFlightId() + "+" + request.getCabinClass() + "+" + request.getFareType()
            );
        }

        if (request.getValidUntil() != null && request.getValidUntil().isBefore(request.getValidFrom())) {
            throw new ValidationException(ErrorCode.INVALID_FARE_DATES,
                    "Valid until must be after valid from");
        }

        Fare fare = fareMapper.toEntity(request);

        if (fare.getCurrency() == null) {
            fare.setCurrency(pricingProperties.getDefaultCurrency());
        }
        if (fare.getActive() == null) {
            fare.setActive(true);
        }

        Fare saved = fareRepository.save(fare);
        return buildResponse(saved);
    }

    @Override
    public FareResponse getFareById(Long id) {
        Fare fare = fareRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FARE_NOT_FOUND, "Fare", id
                ));
        return buildResponse(fare);
    }

    @Override
    public List<FareResponse> getFaresByFlightId(Long flightId) {
        return fareRepository.findByFlightId(flightId)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    @Override
    public Page<FareResponse> getAllFares(Pageable pageable) {
        return fareRepository.findAll(pageable)
                .map(this::buildResponse);
    }

    @Override
    @Transactional
    public FareResponse updateFare(Long id, FareRequest request, Long requesterId) {
        Fare existing = fareRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FARE_NOT_FOUND, "Fare", id
                ));

        if (request.getValidUntil() != null && request.getValidFrom() != null
                && request.getValidUntil().isBefore(request.getValidFrom())) {
            throw new ValidationException(ErrorCode.INVALID_FARE_DATES,
                    "Valid until must be after valid from");
        }

        fareMapper.updateEntity(existing, request);
        Fare updated = fareRepository.save(existing);
        return buildResponse(updated);
    }

    @Override
    @Transactional
    public FareResponse changeActiveStatus(Long id, Boolean active, Long requesterId) {
        Fare fare = fareRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FARE_NOT_FOUND, "Fare", id
                ));

        fare.setActive(active);
        Fare updated = fareRepository.save(fare);
        return buildResponse(updated);
    }

    @Override
    @Transactional
    public void deleteFare(Long id, Long requesterId) {
        Fare fare = fareRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FARE_NOT_FOUND, "Fare", id
                ));
        fareRepository.delete(fare);
    }

    private FareResponse buildResponse(Fare fare) {
        FareResponse response = fareMapper.toResponse(fare);
        response.setTotalPrice(fare.getTotalPrice());
        return response;
    }

}
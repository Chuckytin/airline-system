package com.airline.service.impl;

import com.airline.config.SeatProperties;
import com.airline.enums.SeatStatus;
import com.airline.enums.SeatType;
import com.airline.exception.ErrorCode;
import com.airline.exception.ResourceAlreadyExistsException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.exception.ValidationException;
import com.airline.mapper.CabinMapper;
import com.airline.model.Cabin;
import com.airline.model.SeatInstance;
import com.airline.model.SeatMap;
import com.airline.payload.request.CabinRequest;
import com.airline.payload.response.CabinAvailabilityResponse;
import com.airline.payload.response.CabinResponse;
import com.airline.repository.CabinRepository;
import com.airline.repository.SeatInstanceRepository;
import com.airline.repository.SeatMapRepository;
import com.airline.service.CabinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CabinServiceImpl implements CabinService {

    private final CabinRepository cabinRepository;
    private final SeatMapRepository seatMapRepository;
    private final SeatInstanceRepository seatInstanceRepository;
    private final CabinMapper cabinMapper;
    private final SeatProperties seatProperties;

    @Override
    @Transactional
    public CabinResponse createCabin(CabinRequest request, Long requesterId) {

        validateRows(request.getRowStart(), request.getRowEnd());

        if (cabinRepository.existsByFlightInstanceIdAndCabinClass(
                request.getFlightInstanceId(), request.getCabinClass())) {
            throw new ResourceAlreadyExistsException(
                    ErrorCode.CABIN_ALREADY_EXISTS,
                    "Cabin", "flightInstanceId+cabinClass",
                    request.getFlightInstanceId() + "+" + request.getCabinClass()
            );
        }

        Cabin cabin = cabinMapper.toEntity(request);

        cabin.setCurrency(seatProperties.getDefaultCurrency());
        cabin.setActive(seatProperties.getDefaultActive());

        List<String> columnGroups = parseColumnLayout(request.getColumnLayout());
        String allLetters = String.join("", columnGroups);
        int rows = request.getRowEnd() - request.getRowStart() + 1;
        int totalSeats = rows * allLetters.length();

        cabin.setTotalSeats(totalSeats);
        cabin.setAvailableSeats(totalSeats);

        Cabin saved = cabinRepository.save(cabin);

        List<SeatMap> seatMaps = generateSeatMaps(saved, columnGroups, request);
        List<SeatInstance> seatInstances = generateSeatInstances(saved, columnGroups, request);

        seatMapRepository.saveAll(seatMaps);
        seatInstanceRepository.saveAll(seatInstances);

        saved.setSeatMaps(new LinkedHashSet<>(seatMaps));
        saved.setSeatInstances(new LinkedHashSet<>(seatInstances));

        log.info("Cabin {} created with {} seats ({} rows x {} columns)",
                saved.getId(), totalSeats, rows, allLetters.length());

        return cabinMapper.toResponse(saved);
    }

    @Override
    public CabinResponse getCabinById(Long id) {
        Cabin cabin = cabinRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.CABIN_NOT_FOUND, "Cabin", id
                ));
        return cabinMapper.toBasicResponse(cabin);
    }

    @Override
    public CabinResponse getCabinWithDetails(Long id) {
        Cabin cabin = cabinRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.CABIN_NOT_FOUND, "Cabin", id
                ));
        return cabinMapper.toResponse(cabin);
    }

    @Override
    public List<CabinResponse> getCabinsByFlightInstance(Long flightInstanceId) {
        return cabinRepository.findByFlightInstanceIdWithDetails(flightInstanceId)
                .stream()
                .map(cabinMapper::toResponse)
                .toList();
    }

    @Override
    public Page<CabinResponse> getAllCabins(Pageable pageable) {
        return cabinRepository.findAll(pageable)
                .map(cabinMapper::toBasicResponse);
    }

    @Override
    public CabinAvailabilityResponse getAvailability(Long cabinId) {
        Cabin cabin = cabinRepository.findById(cabinId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.CABIN_NOT_FOUND, "Cabin", cabinId
                ));

        List<Object[]> counts = seatInstanceRepository.countByCabinIdGroupedByStatus(cabinId);

        int available = 0, occupied = 0, blocked = 0, reserved = 0;
        for (Object[] row : counts) {
            SeatStatus status = (SeatStatus) row[0];
            long count = ((Number) row[1]).longValue();
            switch (status) {
                case AVAILABLE -> available = (int) count;
                case OCCUPIED -> occupied = (int) count;
                case BLOCKED -> blocked = (int) count;
                case RESERVED -> reserved = (int) count;
            }
        }

        return CabinAvailabilityResponse.builder()
                .cabinId(cabin.getId())
                .flightInstanceId(cabin.getFlightInstanceId())
                .cabinClass(cabin.getCabinClass())
                .totalSeats(cabin.getTotalSeats())
                .availableSeats(available)
                .occupiedSeats(occupied)
                .blockedSeats(blocked)
                .reservedSeats(reserved)
                .build();
    }

    @Override
    @Transactional
    public CabinResponse updateCabin(Long id, CabinRequest request, Long requesterId) {
        Cabin existing = cabinRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.CABIN_NOT_FOUND, "Cabin", id
                ));

        // No permite cambiar layout/rows si ya hay reservas en la cabin
        boolean layoutChanged =
                !existing.getRowStart().equals(request.getRowStart())
                        || !existing.getRowEnd().equals(request.getRowEnd())
                        || !existing.getColumnLayout().equals(request.getColumnLayout());

        if (layoutChanged && seatInstanceRepository.existsByCabinIdAndBookingIdIsNotNull(id)) {
            throw new ValidationException(ErrorCode.INVALID_CABIN_LAYOUT,
                    "Cannot modify layout: cabin already has assigned seats");
        }

        validateRows(request.getRowStart(), request.getRowEnd());

        cabinMapper.updateEntity(existing, request);
        Cabin updated = cabinRepository.save(existing);
        return cabinMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public CabinResponse changeActiveStatus(Long id, Boolean active, Long requesterId) {
        Cabin cabin = cabinRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.CABIN_NOT_FOUND, "Cabin", id
                ));
        cabin.setActive(active);
        Cabin updated = cabinRepository.save(cabin);
        return cabinMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCabin(Long id, Long requesterId) {
        Cabin cabin = cabinRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.CABIN_NOT_FOUND, "Cabin", id
                ));

        if (seatInstanceRepository.existsByCabinIdAndBookingIdIsNotNull(id)) {
            throw new ValidationException(ErrorCode.INVALID_REQUEST,
                    "Cannot delete cabin: it has assigned seats");
        }

        cabinRepository.delete(cabin);
    }

    private void validateRows(Integer rowStart, Integer rowEnd) {
        if (rowStart > rowEnd) {
            throw new ValidationException(ErrorCode.INVALID_CABIN_ROWS,
                    "Row start cannot be greater than row end");
        }
    }

    /**
     * Parsea "ABC-DEF" -> ["ABC", "DEF"]
     * Parsea "AB-CD"  -> ["AB", "CD"]
     */
    private List<String> parseColumnLayout(String columnLayout) {
        List<String> groups = new ArrayList<>();
        for (String group : columnLayout.split("-")) {
            if (group.isEmpty()) {
                throw new ValidationException(ErrorCode.INVALID_CABIN_LAYOUT,
                        "Invalid column layout: empty group");
            }
            groups.add(group);
        }
        return groups;
    }

    private List<SeatMap> generateSeatMaps(Cabin cabin,
                                           List<String> columnGroups,
                                           CabinRequest request) {
        List<SeatMap> seatMaps = new ArrayList<>();
        boolean isExitRow = isExitRow(request.getRowStart(), request.getRowEnd());

        // Layout completo de la fila, sin guiones (normalizado)
        String fullLetters = String.join("", columnGroups);

        for (int row = request.getRowStart(); row <= request.getRowEnd(); row++) {
            seatMaps.add(SeatMap.builder()
                    .cabin(cabin)
                    .seatRow(row)
                    .seatLetters(fullLetters)
                    .hasExtraLegroom(false)
                    .isExitRow(isExitRow)
                    .build());
        }
        return seatMaps;
    }

    private List<SeatInstance> generateSeatInstances(Cabin cabin,
                                                     List<String> columnGroups,
                                                     CabinRequest request) {
        List<SeatInstance> instances = new ArrayList<>();
        boolean isExitRow = isExitRow(request.getRowStart(), request.getRowEnd());
        int totalGroups = columnGroups.size();

        for (int row = request.getRowStart(); row <= request.getRowEnd(); row++) {
            for (int g = 0; g < totalGroups; g++) {
                String group = columnGroups.get(g);
                for (int i = 0; i < group.length(); i++) {
                    String letter = String.valueOf(group.charAt(i));
                    SeatType type = resolveSeatType(g, i, group.length(), totalGroups);

                    instances.add(SeatInstance.builder()
                            .cabin(cabin)
                            .seatNumber(row + letter)
                            .seatRow(row)
                            .columnLetter(letter)
                            .seatType(type)
                            .status(SeatStatus.AVAILABLE)
                            .hasExtraLegroom(false)
                            .isExitRow(isExitRow)
                            .priceModifier(seatProperties.getDefaultPriceModifier())
                            .build());
                }
            }
        }
        return instances;
    }

    /**
     * Determina el tipo de asiento según su posición en el layout completo.
     * Geometría de ejemplo para "ABC-DEF":
     * [ventana] A B C [pasillo] D E F [ventana]
     * Reglas:
     * - Primera letra del primer grupo -> WINDOW
     * - Última letra del último grupo -> WINDOW
     * - Última letra de un grupo que NO es el último -> AISLE (pegado al pasillo izquierdo)
     * - Primera letra de un grupo que NO es el primero -> AISLE (pegado al pasillo derecho)
     * - Resto -> MIDDLE
     *
     */
    private SeatType resolveSeatType(int groupIndex, int letterIndex, int groupSize, int totalGroups) {

        boolean isFirstGroup = groupIndex == 0;
        boolean isLastGroup = groupIndex == totalGroups - 1;

        // Extremos del avión: ventana
        if (isFirstGroup && letterIndex == 0) return SeatType.WINDOW;
        if (isLastGroup && letterIndex == groupSize - 1) return SeatType.WINDOW;

        // Bordes de pasillo
        boolean touchesLeftAisle = !isLastGroup && letterIndex == groupSize - 1;
        boolean touchesRightAisle = !isFirstGroup && letterIndex == 0;

        if (touchesLeftAisle || touchesRightAisle) return SeatType.AISLE;

        // Los demás asientos
        return SeatType.MIDDLE;
    }

    /**
     * Se considera exit row si todoo el rango configurado está dentro del rango de la cabin
     */
    private boolean isExitRow(Integer rowStart, Integer rowEnd) {
        Integer exitStart = seatProperties.getExitRowStart();
        Integer exitEnd = seatProperties.getExitRowEnd();
        if (exitStart == null || exitEnd == null || exitStart == 0 || exitEnd == 0) {
            return false;
        }

        return rowStart <= exitStart && rowEnd >= exitEnd;
    }

}
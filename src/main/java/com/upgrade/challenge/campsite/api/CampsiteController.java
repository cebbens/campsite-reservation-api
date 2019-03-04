package com.upgrade.challenge.campsite.api;

import com.upgrade.challenge.campsite.api.availability.AvailabilityDto;
import com.upgrade.challenge.campsite.api.common.ApiResponse;
import com.upgrade.challenge.campsite.api.common.ApiResponseEntity;
import com.upgrade.challenge.campsite.api.common.exceptions.CheckInAfterCheckOutException;
import com.upgrade.challenge.campsite.api.common.exceptions.MaxReservationDaysExceededException;
import com.upgrade.challenge.campsite.api.common.exceptions.ReservationDaysAheadOutOfBoundsException;
import com.upgrade.challenge.campsite.api.reservation.ReservationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.*;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "${api.base-path}/campsites", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class CampsiteController {

    @Value("${api.campsite.reservation.max-days}")
    private Integer reservationMaxDays;

    @Value("${api.campsite.reservation.min-days-ahead}")
    private Integer reservationMinDaysAhead;

    @Value("${api.campsite.reservation.max-days-ahead}")
    private Integer reservationMaxDaysAhead;

    @Value("${api.campsite.availability.range-threshold}")
    private Integer availabilityRangeThreshold;

    private final CampsiteService campsiteService;

    @PostMapping
    public ResponseEntity<ApiResponse<CampsiteDto>> create(@Validated(CampsiteDto.CreateValidation.class) @RequestBody CampsiteDto campsiteDto) {
        return ApiResponseEntity.<CampsiteDto>builder().created(campsiteService.create(campsiteDto)).build();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CampsiteDto>>> getAll(Pageable pageable) {
        return ApiResponseEntity.<List<CampsiteDto>>builder().ok(campsiteService.getAll(pageable)).build();
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<ApiResponse<CampsiteDto>> get(@PathVariable UUID id) {
        return ApiResponseEntity.<CampsiteDto>builder().ok(campsiteService.get(id)).build();
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<ApiResponse<CampsiteDto>> update(@PathVariable UUID id, @Validated(CampsiteDto.UpdateValidation.class) @RequestBody CampsiteDto campsiteDto) {
        return ApiResponseEntity.<CampsiteDto>builder().ok(campsiteService.update(id, campsiteDto)).build();
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        return ApiResponseEntity.<Void>builder().ok(campsiteService.delete(id)).build();
    }

    @GetMapping(path = "/{id}/availabilities")
    public ResponseEntity<ApiResponse<List<AvailabilityDto>>> getAvailabilities(@PathVariable UUID id,
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        if (start == null) {
            start = LocalDate.now();
        }

        // Date range with the default being 'api.campsite.availability.range-threshold' (30) days (1 month)
        if (end == null || DAYS.between(start, end) > availabilityRangeThreshold) {
            end = start.plusDays(availabilityRangeThreshold);
        }

        return ApiResponseEntity.<List<AvailabilityDto>>builder().ok(campsiteService.getAvailabilities(id, start, end)).build();
    }

    @PostMapping(path = "/{id}/reservations")
    public ResponseEntity<ApiResponse<ReservationDto>> makeReservation(@PathVariable UUID id, @Valid @RequestBody ReservationDto reservationDto) {
        if (reservationDto.getCheckIn().isAfter(reservationDto.getCheckOut())) {
            throw new CheckInAfterCheckOutException(reservationDto.getCheckIn(), reservationDto.getCheckOut());
        }

        // The campsite can be reserved for max 'api.campsite.max-reservation-days' (3) day(s)
        if (DAYS.between(reservationDto.getCheckIn(), reservationDto.getCheckOut()) > reservationMaxDays) {
            throw new MaxReservationDaysExceededException(reservationMaxDays);
        }

        long reservationDaysAhead = DAYS.between(LocalDate.now(), reservationDto.getCheckIn());
        // The campsite can be reserved minimum 'api.campsite.reservation.min-days-ahead' (1) day(s) ahead of arrival and up to 'api.campsite.reservation.max-days-ahead' (30) day(s) in advance
        if (reservationDaysAhead < reservationMinDaysAhead || reservationDaysAhead > reservationMaxDaysAhead) {
            throw new ReservationDaysAheadOutOfBoundsException(reservationMinDaysAhead, reservationMaxDaysAhead);
        }

        return ApiResponseEntity.<ReservationDto>builder().created(campsiteService.makeReservation(id, reservationDto)).build();
    }

    @GetMapping(path = "/{id}/reservations")
    public ResponseEntity<ApiResponse<List<ReservationDto>>> getAllReservations(@PathVariable UUID id, Pageable pageable) {
        return ApiResponseEntity.<List<ReservationDto>>builder().ok(campsiteService.getAllReservations(id, pageable)).build();
    }

    @GetMapping(path = "/{id}/reservations/{reservationId}")
    public ResponseEntity<ApiResponse<ReservationDto>> getReservation(@PathVariable UUID id, @PathVariable UUID reservationId) {
        return ApiResponseEntity.<ReservationDto>builder().ok(campsiteService.getReservation(id, reservationId)).build();
    }

    @PutMapping(path = "/{id}/reservations/{reservationId}")
    public ResponseEntity<ApiResponse<ReservationDto>> modifyReservation(@PathVariable UUID id, @PathVariable UUID reservationId, @Validated @RequestBody ReservationDto reservationDto) {
        if (reservationDto.getCheckIn().isAfter(reservationDto.getCheckOut())) {
            throw new CheckInAfterCheckOutException(reservationDto.getCheckIn(), reservationDto.getCheckOut());
        }

        return ApiResponseEntity.<ReservationDto>builder().ok(campsiteService.modifyReservation(id, reservationId, reservationDto)).build();
    }

    @DeleteMapping(path = "/{id}/reservations/{reservationId}")
    public ResponseEntity<ApiResponse<Void>> cancelReservation(@PathVariable UUID id, @PathVariable UUID reservationId) {
        return ApiResponseEntity.<Void>builder().ok(campsiteService.cancelReservation(id, reservationId)).build();
    }
}

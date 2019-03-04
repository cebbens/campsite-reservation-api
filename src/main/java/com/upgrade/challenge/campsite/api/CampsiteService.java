package com.upgrade.challenge.campsite.api;

import com.upgrade.challenge.campsite.api.availability.Availability;
import com.upgrade.challenge.campsite.api.availability.AvailabilityConverter;
import com.upgrade.challenge.campsite.api.availability.AvailabilityDto;
import com.upgrade.challenge.campsite.api.availability.AvailabilityRepository;
import com.upgrade.challenge.campsite.api.common.ApiResponse;
import com.upgrade.challenge.campsite.api.common.exceptions.EntityNotFoundException;
import com.upgrade.challenge.campsite.api.common.exceptions.NotAvailableSiteException;
import com.upgrade.challenge.campsite.api.reservation.Reservation;
import com.upgrade.challenge.campsite.api.reservation.ReservationConverter;
import com.upgrade.challenge.campsite.api.reservation.ReservationDto;
import com.upgrade.challenge.campsite.api.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@CommonsLog
@RequiredArgsConstructor
@Transactional
@Service
public class CampsiteService {

    private final CampsiteRepository campsiteRepository;
    private final CampsiteConverter campsiteConverter;
    private final ReservationRepository reservationRepository;
    private final ReservationConverter reservationConverter;
    private final AvailabilityRepository availabilityRepository;
    private final AvailabilityConverter availabilityConverter;

    public ApiResponse<CampsiteDto> create(CampsiteDto campsiteDto) {
        Campsite campsite = campsiteRepository.save(campsiteConverter.toEntity(campsiteDto));

        this.initCampsiteAvailability(campsite);

        return ApiResponse.<CampsiteDto>builder().data(campsiteConverter.toDto(campsite)).build();
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<CampsiteDto>> getAll(Pageable pageable) {
        return ApiResponse.<List<CampsiteDto>>builder().data(campsiteRepository.findAll(pageable).map(campsiteConverter::toDto).getContent()).build();
    }

    @Transactional(readOnly = true)
    public ApiResponse<CampsiteDto> get(UUID id) {
        return ApiResponse.<CampsiteDto>builder().data(campsiteRepository.findById(id).map(campsiteConverter::toDto)
                .orElseThrow(() -> new EntityNotFoundException(Campsite.class, id))).build();
    }

    /**
     * Updating the capacity is not supported because the available sites are already set for the Campsite.
     *
     * @param id Campsite ID.
     * @param campsiteDto Campsite DTO.
     * @return ApiResponse
     */
    public ApiResponse<CampsiteDto> update(UUID id, CampsiteDto campsiteDto) {
        Campsite campsite = campsiteRepository.getOne(id);

        campsiteConverter.toEntity(campsiteDto, campsite);

        return ApiResponse.<CampsiteDto>builder().data(campsiteConverter.toDto(campsite)).build();
    }

    public ApiResponse<Void> delete(UUID id) {
        campsiteRepository.deleteById(id);
        return ApiResponse.<Void>builder().build();
    }

    /**
     * Initializes a year's availability for each Campsite at server startup.
     */
    @Scheduled(fixedRateString = "P365D")
    public void initAvailability() {
        campsiteRepository.findAll().forEach(this::initCampsiteAvailability);
    }

    /**
     * Initializes the availability for a Campsite. It sets Availability's sites to the Campsite capacity and determines
     * the base date from which to initialize it.
     */
    private void initCampsiteAvailability(Campsite campsite) {
        List<Availability> availabilities = campsite.getAvailabilities();

        LocalDate baseDate;
        if (availabilities.isEmpty()) {
            baseDate = LocalDate.now();
        }
        // If already initialized and today is after the last availability date, then it initializes the availability from today
        else {
            baseDate = availabilities.get(availabilities.size() - 1).getDate();
            if (LocalDate.now().isBefore(baseDate)) {
                return;
            }
        }

        IntStream.range(0, YearMonth.from(baseDate).lengthOfYear())
                .mapToObj(baseDate::plusDays)
                .forEach(date -> campsite.addAvailability(Availability.builder().date(date).sites(campsite.getCapacity()).build()));
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<AvailabilityDto>> getAvailabilities(UUID id, LocalDate start, LocalDate end) {
        log.debug(String.format("Availability [start: %s, end: %s]", start, end));

        List<AvailabilityDto> availabilityDtos = availabilityRepository.findAllProjectedByCampsiteIdAndDateBetween(id, start, end);

        return ApiResponse.<List<AvailabilityDto>>builder().data(availabilityDtos).build();
    }

    public ApiResponse<ReservationDto> makeReservation(UUID id, ReservationDto reservationDto) {
        List<Availability> newAvailabilities = this.checkAvailabilities(id, reservationDto.getCheckIn(), reservationDto.getCheckOut());

        Reservation reservation = reservationRepository.save(reservationConverter.toEntity(reservationDto));

        campsiteRepository.getOne(id).addReservation(reservation);

        newAvailabilities.forEach(availability -> availability.addReservation(reservation));

        return ApiResponse.<ReservationDto>builder().data(reservationConverter.toDto(reservation)).build();
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<ReservationDto>> getAllReservations(UUID id, Pageable pageable) {
        return ApiResponse.<List<ReservationDto>>builder().data(reservationConverter.toDtos(reservationRepository.findAllByCampsiteId(id, pageable).getContent())).build();
    }

    @Transactional(readOnly = true)
    public ApiResponse<ReservationDto> getReservation(UUID id, UUID reservationId) {
        return ApiResponse.<ReservationDto>builder().data(reservationRepository.findOneByIdAndCampsiteId(reservationId, id).map(reservationConverter::toDto)
                .orElseThrow(() -> new EntityNotFoundException(Reservation.class, reservationId))).build();
    }

    public ApiResponse<ReservationDto> modifyReservation(UUID id, UUID reservationId, ReservationDto reservationDto) {
        Reservation reservation = reservationRepository.findOneByIdAndCampsiteId(reservationId, id)
                .orElseThrow(() -> new EntityNotFoundException(Reservation.class, reservationId));

        if (!reservationDto.getCheckIn().isEqual(reservation.getCheckIn()) || !reservationDto.getCheckOut().isEqual(reservation.getCheckOut())) {
            List<Availability> newAvailabilities = this.checkAvailabilities(id, reservationDto.getCheckIn(), reservationDto.getCheckOut());

            availabilityRepository.findAllByCampsiteIdAndDateBetween(id, reservation.getCheckIn(), reservation.getCheckOut())
                    .forEach(availability -> availability.removeReservation(reservation));

            newAvailabilities.forEach(availability -> availability.addReservation(reservation));
        }

        reservationRepository.save(reservationConverter.toEntity(reservationDto, reservation));

        return ApiResponse.<ReservationDto>builder().data(reservationConverter.toDto(reservation)).build();
    }

    public ApiResponse<Void> cancelReservation(UUID id, UUID reservationId) {
        Reservation reservation = reservationRepository.findOneByIdAndCampsiteId(reservationId, id)
                .orElseThrow(() -> new EntityNotFoundException(Reservation.class, reservationId));

        availabilityRepository.findAllByCampsiteIdAndDateBetween(id, reservation.getCheckIn(), reservation.getCheckOut())
                .forEach(availability -> availability.removeReservation(reservation));

        reservation.getCampsite().removeReservation(reservation);

        return ApiResponse.<Void>builder().build();
    }

    private List<Availability> checkAvailabilities(UUID id, LocalDate checkIn, LocalDate checkOut) {
        List<Availability> availabilities = availabilityRepository.findAllByCampsiteIdAndDateBetween(id, checkIn, checkOut);

        if (availabilities.stream().anyMatch(availability -> availability.getSites() == 0)) {
            throw new NotAvailableSiteException(availabilityConverter.toDtos(availabilities));
        }

        return availabilities;
    }
}

package com.upgrade.challenge.campsite.api.reservation;

import com.upgrade.challenge.campsite.api.common.Converter;
import com.upgrade.challenge.campsite.api.common.Helper;
import org.springframework.stereotype.Component;

@Component
public class ReservationConverter implements Converter<ReservationDto, Reservation> {

    @Override
    public Reservation toEntity(ReservationDto dto) {
        return Reservation.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .checkIn(dto.getCheckIn())
                .checkOut(dto.getCheckOut())
                .build();
    }

    @Override
    public Reservation toEntity(ReservationDto dto, Reservation entity) {
        Helper.setIfNotNull(entity::setName, dto.getName());
        Helper.setIfNotNull(entity::setEmail, dto.getEmail());
        Helper.setIfNotNull(entity::setCheckIn, dto.getCheckIn());
        Helper.setIfNotNull(entity::setCheckOut, dto.getCheckOut());

        return entity;
    }

    @Override
    public ReservationDto toDto(Reservation entity) {
        return ReservationDto.builder()
                .id(entity.getId())
                .created(entity.getCreated())
                .updated(entity.getUpdated())
                .name(entity.getName())
                .email(entity.getEmail())
                .checkIn(entity.getCheckIn())
                .checkOut(entity.getCheckOut())
                .build();
    }
}

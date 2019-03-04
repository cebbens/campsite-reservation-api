package com.upgrade.challenge.campsite.api;

import com.upgrade.challenge.campsite.api.common.Converter;
import com.upgrade.challenge.campsite.api.common.Helper;
import com.upgrade.challenge.campsite.api.reservation.ReservationConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CampsiteConverter implements Converter<CampsiteDto, Campsite> {

    private final ReservationConverter reservationConverter;

    @Override
    public Campsite toEntity(CampsiteDto dto) {
        return Campsite.builder()
                .name(dto.getName())
                .capacity(dto.getCapacity())
                .build();
    }

    @Override
    public Campsite toEntity(CampsiteDto dto, Campsite entity) {
        Helper.setIfNotNull(entity::setName, dto.getName());

        return entity;
    }

    @Override
    public CampsiteDto toDto(Campsite entity) {
        return CampsiteDto.builder()
                .id(entity.getId())
                .created(entity.getCreated())
                .updated(entity.getUpdated())
                .name(entity.getName())
                .capacity(entity.getCapacity())
                .reservations(reservationConverter.toDtos(entity.getReservations()))
                .build();
    }
}

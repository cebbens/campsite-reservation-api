package com.upgrade.challenge.campsite.api.availability;

import com.upgrade.challenge.campsite.api.common.Converter;
import com.upgrade.challenge.campsite.api.common.Helper;
import org.springframework.stereotype.Component;

@Component
public class AvailabilityConverter implements Converter<AvailabilityDto, Availability> {

    @Override
    public Availability toEntity(AvailabilityDto dto) {
        return Availability.builder()
                .date(dto.getDate())
                .build();
    }

    @Override
    public Availability toEntity(AvailabilityDto dto, Availability entity) {
        Helper.setIfNotNull(entity::setDate, dto.getDate());

        return entity;
    }

    @Override
    public AvailabilityDto toDto(Availability entity) {
        return AvailabilityDto.builder()
                .date(entity.getDate())
                .sites(entity.getSites())
                .build();
    }
}

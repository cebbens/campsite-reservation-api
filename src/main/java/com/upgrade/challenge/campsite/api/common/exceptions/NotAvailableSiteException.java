package com.upgrade.challenge.campsite.api.common.exceptions;

import com.upgrade.challenge.campsite.api.availability.AvailabilityDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class NotAvailableSiteException extends RuntimeException {

    private final transient List<AvailabilityDto> availabilitiesDtos;
}

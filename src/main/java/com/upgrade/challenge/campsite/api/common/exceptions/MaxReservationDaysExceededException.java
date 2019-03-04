package com.upgrade.challenge.campsite.api.common.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MaxReservationDaysExceededException extends RuntimeException {

    private final Integer reservationMaxDays;
}

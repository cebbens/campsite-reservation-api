package com.upgrade.challenge.campsite.api.common.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReservationDaysAheadOutOfBoundsException extends RuntimeException {

    private final Integer reservationMinDaysAhead;
    private final Integer reservationMaxDaysAhead;
}

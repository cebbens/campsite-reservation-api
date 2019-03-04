package com.upgrade.challenge.campsite.api.common.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class CheckInAfterCheckOutException extends RuntimeException {

    private final LocalDate checkIn;
    private final LocalDate checkOut;
}

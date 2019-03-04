package com.upgrade.challenge.campsite.api.common;

import com.upgrade.challenge.campsite.api.common.exceptions.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestControllerAdvice
public class ApiExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiResponse> handle(MethodArgumentTypeMismatchException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.builder().errors(
                Collections.singletonList(exception.getCause().getMessage())).build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handle(MethodArgumentNotValidException exception) {
        List<String> errors = exception.getBindingResult().getAllErrors().stream().map(objectError ->
                messageSource.getMessage(objectError, LocaleContextHolder.getLocale())).collect(Collectors.toList());
        return ResponseEntity.badRequest().body(ApiResponse.builder().message(HttpStatus.BAD_REQUEST.getReasonPhrase()).errors(errors).build());
    }

    @ExceptionHandler(javax.persistence.EntityNotFoundException.class)
    ResponseEntity<ApiResponse> handle(javax.persistence.EntityNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.builder().errors(Collections.singletonList(
                exception.getMessage() != null ? exception.getMessage() : this.getMessage(exception))).build());
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    ResponseEntity<ApiResponse> handle(EmptyResultDataAccessException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.builder().errors(
                Collections.singletonList(exception.getMessage())).build());
    }

    @ExceptionHandler(DataAccessException.class)
    ResponseEntity<ApiResponse> handle(DataAccessException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.builder().errors(
                Collections.singletonList(exception.getMessage())).build());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    ResponseEntity<ApiResponse> handle(EntityNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.builder().errors(Collections.singletonList(
                this.getMessage(exception, exception.getEntity().getSimpleName(), exception.getId()))).build());
    }

    @ExceptionHandler(NotAvailableSiteException.class)
    ResponseEntity<ApiResponse> handle(NotAvailableSiteException exception) {
        return ResponseEntity.unprocessableEntity().body(ApiResponse.builder().errors(
                Collections.singletonList(this.getMessage(exception))).data(exception.getAvailabilitiesDtos()).build());
    }

    @ExceptionHandler(CheckInAfterCheckOutException.class)
    ResponseEntity<ApiResponse> handle(CheckInAfterCheckOutException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.builder().errors(Collections.singletonList(
                this.getMessage(exception, exception.getCheckIn(), exception.getCheckOut()))).build());
    }

    @ExceptionHandler(MaxReservationDaysExceededException.class)
    ResponseEntity<ApiResponse> handle(MaxReservationDaysExceededException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.builder().errors(
                Collections.singletonList(this.getMessage(exception, exception.getReservationMaxDays()))).build());
    }

    @ExceptionHandler(ReservationDaysAheadOutOfBoundsException.class)
    ResponseEntity<ApiResponse> handle(ReservationDaysAheadOutOfBoundsException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.builder().errors(
                Collections.singletonList(this.getMessage(exception, exception.getReservationMinDaysAhead(),
                        exception.getReservationMaxDaysAhead()))).build());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse> handle(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.builder().errors(
                Collections.singletonList(exception.getMessage())).build());
    }

    private String getMessage(RuntimeException exception, Object... args) {
        return messageSource.getMessage(exception.getClass().getSimpleName(), args, LocaleContextHolder.getLocale());
    }
}

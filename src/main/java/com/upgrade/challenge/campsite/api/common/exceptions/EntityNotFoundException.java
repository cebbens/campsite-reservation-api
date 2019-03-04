package com.upgrade.challenge.campsite.api.common.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class EntityNotFoundException extends RuntimeException {

    private final Class entity;
    private final UUID id;
}

package com.upgrade.challenge.campsite.api.common;

import java.util.List;
import java.util.stream.Collectors;

public interface Converter<D, E> {

    E toEntity(D dto);
    E toEntity(D dto, E entity);
    D toDto(E entity);

    default List<E> toEntities(List<D> dtos) {
        return dtos.stream().map(this::toEntity).collect(Collectors.toList());
    }

    default List<D> toDtos(List<E> entities) {
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }
}

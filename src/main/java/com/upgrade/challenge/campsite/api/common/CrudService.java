package com.upgrade.challenge.campsite.api.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Transactional
public abstract class CrudService<D, E extends BaseEntity> {

    public E create(D dto) {
        E entity = this.createEntityInstance();

        this.getConverter().toEntity(dto);

        this.preCreate(dto, entity);

        entity = this.getRepository().save(entity);

        return entity;
    }

    @Transactional(readOnly = true)
    public D get(UUID id) {
        return this.getRepository().findById(id).map(this.getConverter()::toDto).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<D> getAll() {
        List<E> entities = this.getRepository().findAll();

        return entities.stream().map(this.getConverter()::toDto).collect(Collectors.toCollection(() -> new ArrayList<>(entities.size())));
    }

    public Boolean update(UUID id, D dto) {
        Optional<E> optional = this.getRepository().findById(id);

        if (optional.isPresent()) {
            E entity = optional.get();

            this.getConverter().toEntity(dto);

            this.preUpdate(dto, entity);

            this.getRepository().save(entity);

            return true;
        }

        return false;
    }

    public Boolean delete(UUID id) {
        this.getRepository().deleteById(id);

        return true;
    }

    protected abstract JpaRepository<E, UUID> getRepository();

    protected abstract Converter<D, E> getConverter();

    protected abstract E createEntityInstance();

    protected void preCreate(D dto, E entity) {
    }

    protected void preUpdate(D dto, E entity) {
    }
}

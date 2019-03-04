package com.upgrade.challenge.campsite.api.reservation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    Optional<Reservation> findOneByIdAndCampsiteId(UUID id, UUID campsiteId);

    @Query(value = "select * from reservations r where r.campsite_id = :campsiteId order by r.created", nativeQuery = true)
    Page<Reservation> findAllByCampsiteId(@NonNull UUID campsiteId, Pageable pageable);
}

package com.upgrade.challenge.campsite.api.availability;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, UUID> {

//    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
//    @Query(value = "from Availability a where a.campsite.id = :campsiteId and (a.date >= :start and a.date < :end) order by a.date")
    @Query(value = "select * from availabilities a where a.campsite_id = :campsiteId and (a.date >= :start and a.date < :end) order by a.date", nativeQuery = true)
    List<Availability> findAllByCampsiteIdAndDateBetween(@Param("campsiteId") UUID campsiteId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query(nativeQuery = true)
    List<AvailabilityDto> findAllProjectedByCampsiteIdAndDateBetween(@Param("campsiteId") UUID campsiteId, @Param("start") LocalDate start, @Param("end") LocalDate end);
}

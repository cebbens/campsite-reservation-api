package com.upgrade.challenge.campsite.api.availability;

import com.upgrade.challenge.campsite.api.Campsite;
import com.upgrade.challenge.campsite.api.common.BaseEntity;
import com.upgrade.challenge.campsite.api.reservation.Reservation;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Entity
@NamedNativeQuery(name = "Availability.findAllProjectedByCampsiteIdAndDateBetween", resultClass = AvailabilityDto.class, resultSetMapping = "AvailabilityDto",
        query = "select a.date as date, a.sites as sites from availabilities a where a.campsite_id = :campsiteId and (a.date between :start and :end) order by a.date")
@SqlResultSetMapping(name = "AvailabilityDto",
        classes = @ConstructorResult(targetClass = AvailabilityDto.class, columns = {@ColumnResult(name = "date", type = LocalDate.class), @ColumnResult(name = "sites", type = Integer.class)}))
@Table(name = "availabilities")
public class Availability extends BaseEntity {

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Integer sites;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campsite_id")
    private Campsite campsite;

    @Builder.Default
    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "reservations_availabilities",
            joinColumns = @JoinColumn(name = "reservation_id"),
            inverseJoinColumns = @JoinColumn(name = "availability_id"))
    private Set<Reservation> reservations = new HashSet<>();

    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
        reservation.getAvailabilities().add(this);
        sites--;
    }

    public void removeReservation(Reservation reservation) {
        reservations.remove(reservation);
        reservation.getAvailabilities().remove(this);
        sites++;
    }
}

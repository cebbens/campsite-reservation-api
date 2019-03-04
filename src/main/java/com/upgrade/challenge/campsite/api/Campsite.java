package com.upgrade.challenge.campsite.api;

import com.upgrade.challenge.campsite.api.availability.Availability;
import com.upgrade.challenge.campsite.api.common.BaseEntity;
import com.upgrade.challenge.campsite.api.reservation.Reservation;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Entity
@Table(name = "campsites")
public class Campsite extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer capacity;

    @Builder.Default
    @OneToMany(mappedBy = "campsite", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Availability> availabilities = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "campsite", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();

    public void addAvailability(Availability availability) {
        availabilities.add(availability);
        availability.setCampsite(this);
    }

    public void removeAvailability(Availability availability) {
        availabilities.remove(availability);
        availability.setCampsite(null);
    }

    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
        reservation.setCampsite(this);
    }

    public void removeReservation(Reservation reservation) {
        reservations.remove(reservation);
        reservation.setCampsite(null);
    }
}

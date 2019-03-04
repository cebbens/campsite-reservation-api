package com.upgrade.challenge.campsite.api.reservation;

import com.upgrade.challenge.campsite.api.Campsite;
import com.upgrade.challenge.campsite.api.availability.Availability;
import com.upgrade.challenge.campsite.api.common.BaseEntity;
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
@Table(name = "reservations")
public class Reservation extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private LocalDate checkIn;

    @Column(nullable = false)
    private LocalDate checkOut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campsite_id")
    private Campsite campsite;

    @Builder.Default
    @ManyToMany(mappedBy = "reservations")
    private Set<Availability> availabilities = new HashSet<>();
}

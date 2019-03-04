package com.upgrade.challenge.campsite.api.availability;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityDto {

    @NotNull
    private LocalDate date;

    @NotNull
    @Positive
    private Integer sites;
}

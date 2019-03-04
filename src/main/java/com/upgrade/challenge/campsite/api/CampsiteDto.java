package com.upgrade.challenge.campsite.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.upgrade.challenge.campsite.api.reservation.ReservationDto;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "reservations")
public class CampsiteDto {

    private UUID id;

    private LocalDateTime created;

    private LocalDateTime updated;

    @NotBlank(groups = {CreateValidation.class, UpdateValidation.class})
    private String name;

    @Null(groups = UpdateValidation.class)
    @NotNull(groups = CreateValidation.class)
    @Positive(groups = CreateValidation.class)
    private Integer capacity;

    @JsonIgnore
    private List<ReservationDto> reservations;


    public interface CreateValidation {
    }

    public interface UpdateValidation {
    }
}

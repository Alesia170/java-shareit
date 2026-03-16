package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDtoRequest {

    @NotNull
    @Future
    private LocalDateTime start;

    @NotNull
    @Future
    private LocalDateTime end;

    @NotNull
    private Long itemId;
}

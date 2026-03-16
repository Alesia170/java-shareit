package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.Status;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDtoResponse {

    private Long id;

    private LocalDateTime start;

    private LocalDateTime end;

    private ItemDto item;

    private UserDto booker;

    private Status status;
}

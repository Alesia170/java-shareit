package ru.practicum.shareit.item;

import java.time.LocalDateTime;

public interface ItemBookingProjection {
    Long getId();

    String getName();

    String getDescription();

    Boolean getAvailable();

    Long getLastBookingId();

    Long getLastBookerId();

    LocalDateTime getLastStart();

    LocalDateTime getLastEnd();

    Long getNextBookingId();

    Long getNextBookerId();

    LocalDateTime getNextStart();

    LocalDateTime getNextEnd();
}

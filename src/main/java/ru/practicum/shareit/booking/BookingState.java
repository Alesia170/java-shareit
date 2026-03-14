package ru.practicum.shareit.booking;

import ru.practicum.shareit.exception.ValidationException;

import java.util.Optional;

public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static BookingState from(String state) {
        return Optional.ofNullable(state)
                .filter(st -> !st.isBlank())
                .map(st -> {
                    try {
                        return BookingState.valueOf(state.trim().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new ValidationException("Неизвестное состояние бронирования " + state);
                    }
                })
                .orElse(ALL);
    }
}
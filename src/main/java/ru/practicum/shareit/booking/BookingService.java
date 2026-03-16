package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;

import java.util.List;

public interface BookingService {

    BookingDtoResponse saveNewBooking(Long userId, BookingDtoRequest bookingDtoRequest);

    BookingDtoResponse approveBooking(Long userId, Long bookingId, boolean approved);

    BookingDtoResponse getBookingById(Long userId, Long bookingId);

    List<BookingDtoResponse> getUserBookings(Long userId, BookingState state);

    List<BookingDtoResponse> getOwnerBookings(Long userId, BookingState state);
}

package ru.practicum.shareit.booking.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.Booking;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BookingMapper {
    public static BookingDtoResponse toBookingDto(Booking booking) {
        return new BookingDtoResponse(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                new ItemDto(
                        booking.getItem().getId(),
                        booking.getItem().getName()
                ),
                new UserDto(
                        booking.getBooker().getId(),
                        booking.getBooker().getName()
                ),
                booking.getStatus()
        );
    }

    public static Booking toBooking(BookingDtoRequest bookingDtoRequest) {
        Booking booking = new Booking();
        booking.setStart(bookingDtoRequest.getStart());
        booking.setEnd(bookingDtoRequest.getEnd());
        return booking;
    }

    public static BookingDto toBook(Booking booking) {
        if (booking == null) {
            return null;
        }

        return new BookingDto(
                booking.getId(),
                booking.getBooker().getId(),
                booking.getStart(),
                booking.getEnd()
        );
    }
}

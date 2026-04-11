package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BookingMapperTest {

    @Test
    void shouldMapBookingToBookingDtoResponse() {
        User booker = new User();
        booker.setId(3L);
        booker.setName("Алеся");

        User owner = new User();
        owner.setId(1L);
        owner.setName("Владелец");

        Item item = new Item();
        item.setId(7L);
        item.setName("Дрель");
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(100L);
        booking.setStart(LocalDateTime.of(2026, 3, 23, 12, 0));
        booking.setEnd(LocalDateTime.of(2026, 3, 24, 12, 0));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.APPROVED);

        BookingDtoResponse dto = BookingMapper.toBookingDto(booking);

        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2026, 3, 23, 12, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2026, 3, 24, 12, 0));
        assertThat(dto.getStatus()).isEqualTo(Status.APPROVED);

        assertThat(dto.getItem().getId()).isEqualTo(7L);
        assertThat(dto.getItem().getName()).isEqualTo("Дрель");

        assertThat(dto.getBooker().getId()).isEqualTo(3L);
        assertThat(dto.getBooker().getName()).isEqualTo("Алеся");
    }

    @Test
    void shouldMapBookingDtoRequestToBooking() {
        BookingDtoRequest dtoRequest = new BookingDtoRequest();
        dtoRequest.setStart(LocalDateTime.of(2026, 3, 25, 10, 0));
        dtoRequest.setEnd(LocalDateTime.of(2026, 3, 26, 10, 0));

        Booking booking = BookingMapper.toBooking(dtoRequest);

        assertThat(booking.getStart()).isEqualTo(LocalDateTime.of(2026, 3, 25, 10, 0));
        assertThat(booking.getEnd()).isEqualTo(LocalDateTime.of(2026, 3, 26, 10, 0));
    }

    @Test
    void shouldMapBookingToBookingDto() {
        User booker = new User();
        booker.setId(9L);
        booker.setName("Пользователь");

        Booking booking = new Booking();
        booking.setId(15L);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.of(2026, 3, 27, 8, 0));
        booking.setEnd(LocalDateTime.of(2026, 3, 28, 8, 0));

        BookingDto dto = BookingMapper.toBook(booking);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(15L);
        assertThat(dto.getBookerId()).isEqualTo(9L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2026, 3, 27, 8, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2026, 3, 28, 8, 0));
    }
}

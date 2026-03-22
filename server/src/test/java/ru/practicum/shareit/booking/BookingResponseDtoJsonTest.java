package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class BookingResponseDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDtoResponse> json;

    @Test
    void shouldSerializeBookingResponseDto() throws Exception {
        BookingDtoResponse dto = BookingDtoResponse.builder()
                .id(1L)
                .start(LocalDateTime.of(2026, 3, 22, 10, 0, 0))
                .end(LocalDateTime.of(2026, 3, 23, 10, 0, 0))
                .status(Status.APPROVED)
                .build();

        var result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo("2026-03-22T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo("2026-03-23T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.status")
                .isEqualTo("APPROVED");
    }
}

package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemBookingDtoJsonTest {

    @Autowired
    private JacksonTester<ItemBookingDto> json;

    @Test
    void shouldSerializeItemBookingDto() throws Exception {
        BookingDto lastBooking = new BookingDto(
                10L,
                5L,
                LocalDateTime.of(2026, 3, 20, 10, 0, 0),
                LocalDateTime.of(2026, 3, 21, 10, 0, 0)
        );

        BookingDto nextBooking = new BookingDto(
                11L,
                6L,
                LocalDateTime.of(2026, 3, 25, 10, 0, 0),
                LocalDateTime.of(2026, 3, 26, 10, 0, 0)
        );

        ItemBookingDto dto = new ItemBookingDto(
                1L,
                "Дрель",
                "Мощная дрель",
                true,
                lastBooking,
                nextBooking
        );

        String result = json.write(dto).getJson();

        assertThat(result).contains("\"id\":1");
        assertThat(result).contains("\"name\":\"Дрель\"");
        assertThat(result).contains("\"description\":\"Мощная дрель\"");
        assertThat(result).contains("\"available\":true");
        assertThat(result).contains("\"lastBooking\"");
        assertThat(result).contains("\"nextBooking\"");
    }

    @Test
    void shouldDeserializeItemBookingDto() throws Exception {
        String content = "{\n"
                         + "  \"id\": 1,\n"
                         + "  \"name\": \"Дрель\",\n"
                         + "  \"description\": \"Мощная дрель\",\n"
                         + "  \"available\": true,\n"
                         + "  \"lastBooking\": {\n"
                         + "    \"id\": 10,\n"
                         + "    \"bookerId\": 5,\n"
                         + "    \"start\": \"2026-03-20T10:00:00\",\n"
                         + "    \"end\": \"2026-03-21T10:00:00\"\n"
                         + "  },\n"
                         + "  \"nextBooking\": {\n"
                         + "    \"id\": 11,\n"
                         + "    \"bookerId\": 6,\n"
                         + "    \"start\": \"2026-03-25T10:00:00\",\n"
                         + "    \"end\": \"2026-03-26T10:00:00\"\n"
                         + "  }\n"
                         + "}";

        ItemBookingDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Дрель");
        assertThat(dto.getDescription()).isEqualTo("Мощная дрель");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getLastBooking()).isNotNull();
        assertThat(dto.getNextBooking()).isNotNull();
    }
}

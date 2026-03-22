package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.comment.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoOwnerJsonTest {

    @Autowired
    private JacksonTester<ItemOwnerDto> json;

    @Test
    void shouldSerializeItemDtoOwner() throws Exception {
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

        CommentResponseDto comment = CommentResponseDto.builder()
                .id(100L)
                .text("Отличная вещь")
                .authorName("Алеся")
                .created(LocalDateTime.of(2026, 3, 22, 12, 30, 0))
                .build();

        ItemOwnerDto dto = new ItemOwnerDto();
        dto.setId(1L);
        dto.setName("Дрель");
        dto.setDescription("Мощная дрель");
        dto.setAvailable(true);
        dto.setLastBooking(lastBooking);
        dto.setNextBooking(nextBooking);
        dto.setComments(List.of(comment));

        var result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Мощная дрель");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);

        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.id").isEqualTo(10);
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.bookerId").isEqualTo(5);
        assertThat(result).extractingJsonPathStringValue("$.lastBooking.start")
                .isEqualTo("2026-03-20T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.lastBooking.end")
                .isEqualTo("2026-03-21T10:00:00");

        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.id").isEqualTo(11);
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.bookerId").isEqualTo(6);
        assertThat(result).extractingJsonPathStringValue("$.nextBooking.start")
                .isEqualTo("2026-03-25T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.nextBooking.end")
                .isEqualTo("2026-03-26T10:00:00");

        assertThat(result).extractingJsonPathArrayValue("$.comments").hasSize(1);
        assertThat(result).extractingJsonPathNumberValue("$.comments[0].id").isEqualTo(100);
        assertThat(result).extractingJsonPathStringValue("$.comments[0].text").isEqualTo("Отличная вещь");
        assertThat(result).extractingJsonPathStringValue("$.comments[0].authorName").isEqualTo("Алеся");
        assertThat(result).extractingJsonPathStringValue("$.comments[0].created")
                .isEqualTo("2026-03-22T12:30:00");
    }

    @Test
    void shouldDeserializeItemDtoResponse() throws Exception {
        String content = """
                {
                  "id": 1,
                  "name": "Дрель",
                  "description": "Мощная дрель",
                  "available": true,
                  "lastBooking": {
                    "id": 10,
                    "bookerId": 5,
                    "start": "2026-03-20T10:00:00",
                    "end": "2026-03-21T10:00:00"
                  },
                  "nextBooking": {
                    "id": 11,
                    "bookerId": 6,
                    "start": "2026-03-25T10:00:00",
                    "end": "2026-03-26T10:00:00"
                  },
                  "comments": [
                    {
                      "id": 100,
                      "text": "Отличная вещь",
                      "authorName": "Алеся",
                      "created": "2026-03-22T12:30:00"
                    }
                  ]
                }
                """;

        ItemOwnerDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Дрель");
        assertThat(dto.getDescription()).isEqualTo("Мощная дрель");
        assertThat(dto.getAvailable()).isTrue();

        assertThat(dto.getLastBooking()).isNotNull();
        assertThat(dto.getLastBooking().getId()).isEqualTo(10L);
        assertThat(dto.getLastBooking().getBookerId()).isEqualTo(5L);
        assertThat(dto.getLastBooking().getStart())
                .isEqualTo(LocalDateTime.of(2026, 3, 20, 10, 0, 0));
        assertThat(dto.getLastBooking().getEnd())
                .isEqualTo(LocalDateTime.of(2026, 3, 21, 10, 0, 0));

        assertThat(dto.getNextBooking()).isNotNull();
        assertThat(dto.getNextBooking().getId()).isEqualTo(11L);
        assertThat(dto.getNextBooking().getBookerId()).isEqualTo(6L);
        assertThat(dto.getNextBooking().getStart())
                .isEqualTo(LocalDateTime.of(2026, 3, 25, 10, 0, 0));
        assertThat(dto.getNextBooking().getEnd())
                .isEqualTo(LocalDateTime.of(2026, 3, 26, 10, 0, 0));

        assertThat(dto.getComments()).isNotNull();
        assertThat(dto.getComments()).hasSize(1);
        assertThat(dto.getComments().get(0).getId()).isEqualTo(100L);
        assertThat(dto.getComments().get(0).getText()).isEqualTo("Отличная вещь");
        assertThat(dto.getComments().get(0).getAuthorName()).isEqualTo("Алеся");
        assertThat(dto.getComments().get(0).getCreated())
                .isEqualTo(LocalDateTime.of(2026, 3, 22, 12, 30, 0));
    }
}

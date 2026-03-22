package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.comment.CommentResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoResponseJsonTest {

    @Autowired
    private JacksonTester<ItemDtoResponse> json;

    @Test
    void shouldSerializeItemDtoResponse() throws Exception {
        CommentResponseDto comment = new CommentResponseDto(
                100L,
                "Отличная вещь",
                "Алеся",
                LocalDateTime.of(2026, 3, 22, 12, 30, 0)
        );

        ItemDtoResponse dto = new ItemDtoResponse(
                1L,
                "Дрель",
                "Мощная дрель",
                true,
                10L,
                List.of(comment)
        );

        String result = json.write(dto).getJson();

        assertThat(result).contains("\"id\":1");
        assertThat(result).contains("\"name\":\"Дрель\"");
        assertThat(result).contains("\"description\":\"Мощная дрель\"");
        assertThat(result).contains("\"available\":true");
        assertThat(result).contains("\"requestId\":10");
        assertThat(result).contains("\"comments\"");
        assertThat(result).contains("\"text\":\"Отличная вещь\"");
        assertThat(result).contains("\"authorName\":\"Алеся\"");
    }

    @Test
    void shouldDeserializeItemDtoResponse() throws Exception {
        String content = "{\n"
                         + "  \"id\": 1,\n"
                         + "  \"name\": \"Дрель\",\n"
                         + "  \"description\": \"Мощная дрель\",\n"
                         + "  \"available\": true,\n"
                         + "  \"requestId\": 10,\n"
                         + "  \"comments\": [\n"
                         + "    {\n"
                         + "      \"id\": 100,\n"
                         + "      \"text\": \"Отличная вещь\",\n"
                         + "      \"authorName\": \"Алеся\",\n"
                         + "      \"created\": \"2026-03-22T12:30:00\"\n"
                         + "    }\n"
                         + "  ]\n"
                         + "}";

        ItemDtoResponse dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Дрель");
        assertThat(dto.getDescription()).isEqualTo("Мощная дрель");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isEqualTo(10L);
        assertThat(dto.getComments()).hasSize(1);
        assertThat(dto.getComments().get(0).getId()).isEqualTo(100L);
        assertThat(dto.getComments().get(0).getText()).isEqualTo("Отличная вещь");
        assertThat(dto.getComments().get(0).getAuthorName()).isEqualTo("Алеся");
    }
}

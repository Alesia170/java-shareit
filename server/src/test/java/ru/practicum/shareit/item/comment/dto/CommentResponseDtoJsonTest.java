package ru.practicum.shareit.item.comment.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.comment.CommentResponseDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentResponseDtoJsonTest {

    @Autowired
    private JacksonTester<CommentResponseDto> json;

    @Test
    void shouldSerializeCommentResponseDto() throws Exception {
        CommentResponseDto dto = CommentResponseDto.builder()
                .id(1L)
                .text("Отличная вещь")
                .authorName("Алеся")
                .created(LocalDateTime.of(2026, 3, 22, 14, 30, 0))
                .build();

        var result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("Отличная вещь");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("Алеся");
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo("2026-03-22T14:30:00");
    }

    @Test
    void shouldDeserializeCommentResponseDto() throws Exception {
        String content = "{\n"
                         + "  \"id\": 1,\n"
                         + "  \"text\": \"Отличная вещь\",\n"
                         + "  \"authorName\": \"Алеся\",\n"
                         + "  \"created\": \"2026-03-22T14:30:00\"\n"
                         + "}";

        CommentResponseDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getText()).isEqualTo("Отличная вещь");
        assertThat(dto.getAuthorName()).isEqualTo("Алеся");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2026, 3, 22, 14, 30, 0));
    }
}

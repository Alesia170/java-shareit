package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.request.dto.RequestResponseDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class RequestResponseDtoJsonTest {

    @Autowired
    private JacksonTester<RequestResponseDto> json;

    @Test
    void shouldSerializeCreatedFieldWithExpectedFormat() throws Exception {
        RequestResponseDto dto = new RequestResponseDto();
        dto.setId(10L);
        dto.setDescription("Нужна дрель");
        dto.setCreated(LocalDateTime.of(2026, 3, 22, 9, 15, 0));

        var result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo("2026-03-22T09:15:00");
    }
}

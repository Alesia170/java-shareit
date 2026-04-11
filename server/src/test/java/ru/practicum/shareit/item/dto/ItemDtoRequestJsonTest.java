package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoRequestJsonTest {

    @Autowired
    private JacksonTester<ItemDtoRequest> json;

    @Test
    void shouldSerializeItemDtoRequest() throws Exception {
        ItemDtoRequest dto = new ItemDtoRequest("Дрель", "Мощная дрель", true, 1L);

        String result = json.write(dto).getJson();

        assertThat(result).contains("\"name\":\"Дрель\"");
        assertThat(result).contains("\"description\":\"Мощная дрель\"");
        assertThat(result).contains("\"available\":true");
        assertThat(result).contains("\"requestId\":1");
    }

    @Test
    void shouldDeserializeItemDtoRequest() throws Exception {
        String content = "{\n"
                         + "  \"name\": \"Дрель\",\n"
                         + "  \"description\": \"Мощная дрель\",\n"
                         + "  \"available\": true,\n"
                         + "  \"requestId\": 1\n"
                         + "}";

        ItemDtoRequest dto = json.parseObject(content);

        assertThat(dto.getName()).isEqualTo("Дрель");
        assertThat(dto.getDescription()).isEqualTo("Мощная дрель");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isEqualTo(1L);
    }
}

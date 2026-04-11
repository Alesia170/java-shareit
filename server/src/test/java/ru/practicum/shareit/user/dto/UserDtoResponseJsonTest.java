package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoResponseJsonTest {

    @Autowired
    private JacksonTester<UserDtoResponse> json;

    @Test
    void shouldSerializeUserDtoResponse() throws Exception {
        UserDtoResponse dto = new UserDtoResponse(1L, "Alesya", "alesya@email.com");

        String result = json.write(dto).getJson();

        assertThat(result).contains("\"id\":1");
        assertThat(result).contains("\"name\":\"Alesya\"");
        assertThat(result).contains("\"email\":\"alesya@email.com\"");
    }

    @Test
    void shouldDeserializeUserDtoResponse() throws Exception {
        String content = "{\n"
                         + "  \"id\": 1,\n"
                         + "  \"name\": \"Alesya\",\n"
                         + "  \"email\": \"alesya@email.com\"\n"
                         + "}";

        UserDtoResponse dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Alesya");
        assertThat(dto.getEmail()).isEqualTo("alesya@email.com");
    }
}

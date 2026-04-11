package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoRequestJsonTest {

    @Autowired
    private JacksonTester<UserDtoRequest> json;

    @Test
    void shouldSerializeUserDtoRequest() throws Exception {
        UserDtoRequest dto = new UserDtoRequest("Alesya", "alesya@email.com");

        String result = json.write(dto).getJson();

        assertThat(result).contains("\"name\":\"Alesya\"");
        assertThat(result).contains("\"email\":\"alesya@email.com\"");
    }

    @Test
    void shouldDeserializeUserDtoRequest() throws Exception {
        String content = "{\n"
                         + "  \"name\": \"Alesya\",\n"
                         + "  \"email\": \"alesya@email.com\"\n"
                         + "}";

        UserDtoRequest dto = json.parseObject(content);

        assertThat(dto.getName()).isEqualTo("Alesya");
        assertThat(dto.getEmail()).isEqualTo("alesya@email.com");
    }
}

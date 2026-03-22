package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoUpdateJsonTest {

    @Autowired
    private JacksonTester<UserDtoUpdate> json;

    @Test
    void shouldSerializeUserDtoUpdate() throws Exception {
        UserDtoUpdate dto = new UserDtoUpdate("New Name", "new@email.com");

        String result = json.write(dto).getJson();

        assertThat(result).contains("\"name\":\"New Name\"");
        assertThat(result).contains("\"email\":\"new@email.com\"");
    }

    @Test
    void shouldDeserializeUserDtoUpdate() throws Exception {
        String content = "{\n"
                         + "  \"name\": \"New Name\",\n"
                         + "  \"email\": \"new@email.com\"\n"
                         + "}";

        UserDtoUpdate dto = json.parseObject(content);

        assertThat(dto.getName()).isEqualTo("New Name");
        assertThat(dto.getEmail()).isEqualTo("new@email.com");
    }
}

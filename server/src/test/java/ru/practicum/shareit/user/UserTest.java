package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void shouldSetAndGetFields() {
        User user = new User();
        user.setId(1L);
        user.setName("Alesya");
        user.setEmail("alesya@mail.com");

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("Alesya");
        assertThat(user.getEmail()).isEqualTo("alesya@mail.com");
    }

    @Test
    void shouldBeEqualForSameData() {
        User first = new User();
        first.setId(1L);
        first.setName("Alesya");
        first.setEmail("alesya@mail.com");

        User second = new User();
        second.setId(1L);
        second.setName("Alesya");
        second.setEmail("alesya@mail.com");

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    void shouldNotBeEqualForDifferentId() {
        User first = new User();
        first.setId(1L);
        first.setName("Alesya");
        first.setEmail("alesya@mail.com");

        User second = new User();
        second.setId(2L);
        second.setName("Alesya");
        second.setEmail("alesya@mail.com");

        assertThat(first).isNotEqualTo(second);
    }
}

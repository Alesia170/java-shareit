package ru.practicum.shareit.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * TODO Sprint add-controllers.
 */
@Data
public class User {

    private Long id;

    @NotNull(message = "Имя пользователя не должно быть пустым")
    private String name;

    @Email(message = "Неверный формат email")
    @NotBlank
    private String email;
}

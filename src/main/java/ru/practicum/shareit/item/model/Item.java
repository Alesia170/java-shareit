package ru.practicum.shareit.item.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

/**
 * TODO Sprint add-controllers.
 */
@Data
public class Item {

    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    private User owner;

    @NotNull
    private Boolean available;

    private ItemRequest request;
}

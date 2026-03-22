package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemShortDto;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemMapper {
    public static ItemDtoResponse toItemDto(Item item) {
        return new ItemDtoResponse(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null,
                List.of()
        );
    }

    public static Item toItem(ItemDtoRequest itemDtoRequest) {
        Item item = new Item();
        item.setName(itemDtoRequest.getName());
        item.setDescription(itemDtoRequest.getDescription());
        item.setAvailable(itemDtoRequest.getAvailable());
        return item;
    }

    public static ItemShortDto toShortDto(Item item) {
        return new ItemShortDto(item.getId(),
                item.getName(),
                item.getOwner().getId());
    }
}

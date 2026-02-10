package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoUpdate;

import java.util.List;

public interface ItemService {

    List<ItemDtoResponse> getAllItemsByUser(Long userId);

    ItemDtoResponse save(Long userId, ItemDtoRequest itemDtoRequest);

    ItemDtoResponse updateItem(Long userId, Long itemId, ItemDtoUpdate itemDtoUpdate);

    ItemDtoResponse getById(Long itemId);

    List<ItemDtoResponse> getItemBySearch(String text);
}
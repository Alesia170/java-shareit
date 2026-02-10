package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoUpdate;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public List<ItemDtoResponse> getAllItemsByUser(Long userId) {
        return itemRepository.getAllItems()
                .stream()
                .filter(item -> item.getOwner() != null &&
                        item.getOwner().getId().equals(userId))
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public ItemDtoResponse save(Long userId, ItemDtoRequest itemDtoRequest) {
        User owner = userRepository.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Item item = ItemMapper.toItem(itemDtoRequest);
        item.setOwner(owner);
        Item savedItem = itemRepository.saveNewItem(item);

        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDtoResponse updateItem(Long userId, Long itemId, ItemDtoUpdate itemDtoUpdate) {
        User owner = userRepository.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        Item item = itemRepository.getById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        if (!item.getOwner().getId().equals(owner.getId())) {
            throw new ForbiddenException("Редактировать вещь может только владелец");
        }

        if (itemDtoUpdate.getName() != null) {
            item.setName(itemDtoUpdate.getName());
        }

        if (itemDtoUpdate.getDescription() != null) {
            item.setDescription(itemDtoUpdate.getDescription());
        }

        if (itemDtoUpdate.getAvailable() != null) {
            item.setAvailable(itemDtoUpdate.getAvailable());
        }

        Item updatedItem = itemRepository.saveNewItem(item);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDtoResponse getById(Long itemId) {
        Item item = itemRepository.getById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDtoResponse> getItemBySearch(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String searchText = text.toLowerCase();

        return itemRepository.getAllItems()
                .stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> item.getName().toLowerCase().contains(searchText)
                        || (item.getDescription() != null && item.getDescription().toLowerCase().contains(searchText)))
                .map(ItemMapper::toItemDto)
                .toList();
    }
}
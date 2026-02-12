package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemRepository {

    Collection<Item> getAllItems();

    Item saveNewItem(Item item);

    Optional<Item> getById(Long id);
}
package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.Request;
import ru.practicum.shareit.request.dto.ItemShortDto;
import ru.practicum.shareit.user.User;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {

    @Test
    void shouldMapItemToItemDtoResponseWithRequest() {
        User owner = new User();
        owner.setId(1L);

        Request request = new Request();
        request.setId(10L);

        Item item = new Item();
        item.setId(2L);
        item.setName("Дрель");
        item.setDescription("Хорошая дрель");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);

        ItemDtoResponse dto = ItemMapper.toItemDto(item);

        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getName()).isEqualTo("Дрель");
        assertThat(dto.getDescription()).isEqualTo("Хорошая дрель");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isEqualTo(10L);
        assertThat(dto.getComments()).isEmpty();
    }

    @Test
    void shouldMapItemToItemDtoResponseWithoutRequest() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setId(3L);
        item.setName("Молоток");
        item.setDescription("Стальной");
        item.setAvailable(false);
        item.setOwner(owner);
        item.setRequest(null);

        ItemDtoResponse dto = ItemMapper.toItemDto(item);

        assertThat(dto.getId()).isEqualTo(3L);
        assertThat(dto.getName()).isEqualTo("Молоток");
        assertThat(dto.getDescription()).isEqualTo("Стальной");
        assertThat(dto.getAvailable()).isFalse();
        assertThat(dto.getRequestId()).isNull();
        assertThat(dto.getComments()).isEmpty();
    }

    @Test
    void shouldMapItemDtoRequestToItem() {
        ItemDtoRequest dto = new ItemDtoRequest();
        dto.setName("Шуруповерт");
        dto.setDescription("Новый");
        dto.setAvailable(true);

        Item item = ItemMapper.toItem(dto);

        assertThat(item.getName()).isEqualTo("Шуруповерт");
        assertThat(item.getDescription()).isEqualTo("Новый");
        assertThat(item.getAvailable()).isTrue();
    }

    @Test
    void shouldMapItemToShortDto() {
        User owner = new User();
        owner.setId(5L);

        Item item = new Item();
        item.setId(7L);
        item.setName("Пила");
        item.setOwner(owner);

        ItemShortDto dto = ItemMapper.toShortDto(item);

        assertThat(dto.getId()).isEqualTo(7L);
        assertThat(dto.getName()).isEqualTo("Пила");
        assertThat(dto.getOwnerId()).isEqualTo(5L);
    }
}
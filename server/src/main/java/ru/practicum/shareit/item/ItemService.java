package ru.practicum.shareit.item;

import ru.practicum.shareit.item.comment.CommentRequestDto;
import ru.practicum.shareit.item.comment.CommentResponseDto;
import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {

    List<ItemBookingDto> getAllItemsByUser(Long userId);

    ItemDtoResponse save(Long userId, ItemDtoRequest itemDtoRequest);

    ItemDtoResponse updateItem(Long userId, Long itemId, ItemDtoUpdate itemDtoUpdate);

    ItemOwnerDto getById(Long userId, Long itemId);

    List<ItemDtoResponse> getItemBySearch(String text);

    CommentResponseDto addComment(Long userId, Long itemId, CommentRequestDto commentRequestDto);
}
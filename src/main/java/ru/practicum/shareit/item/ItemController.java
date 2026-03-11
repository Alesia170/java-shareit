package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.CommentRequestDto;
import ru.practicum.shareit.item.comment.CommentResponseDto;
import ru.practicum.shareit.item.dto.*;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public List<ItemBookingDto> getAllItemsByUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getAllItemsByUser(userId);
    }

    @PostMapping
    public ItemDtoResponse saveNewItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                       @Valid @RequestBody ItemDtoRequest itemDtoRequest) {
        return itemService.save(userId, itemDtoRequest);
    }

    @PatchMapping("/{itemId}")
    public ItemDtoResponse updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                      @PathVariable Long itemId,
                                      @RequestBody ItemDtoUpdate itemDtoUpdate) {
        return itemService.updateItem(userId, itemId, itemDtoUpdate);
    }

    @GetMapping("/{itemId}")
    public ItemOwnerDto getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                @PathVariable Long itemId) {
        return itemService.getById(userId, itemId);
    }

    @GetMapping("/search")
    public List<ItemDtoResponse> getItemBySearch(@RequestParam String text) {
        return itemService.getItemBySearch(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto addComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @PathVariable Long itemId,
                                         @Valid @RequestBody CommentRequestDto commentRequestDto) {
        return itemService.addComment(userId, itemId, commentRequestDto);
    }
}

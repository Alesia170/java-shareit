package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public List<ItemBookingDto> getAllItemsByUser(Long userId) {
        checkUserExists(userId);

        LocalDateTime now = LocalDateTime.now();

        return itemRepository.findItemsWithBookings(userId, Status.APPROVED, now)
                .stream()
                .map(p -> new ItemBookingDto(
                        p.getId(),
                        p.getName(),
                        p.getDescription(),
                        p.getAvailable(),
                        p.getLastBookingId() == null ? null : new BookingDto(
                                p.getLastBookingId(),
                                p.getLastBookerId(),
                                p.getLastStart(),
                                p.getLastEnd()
                        ),
                        p.getNextBookingId() == null ? null : new BookingDto(
                                p.getNextBookingId(),
                                p.getNextBookerId(),
                                p.getNextStart(),
                                p.getNextEnd()
                        )
                ))
                .toList();
    }

    @Override
    public ItemDtoResponse save(Long userId, ItemDtoRequest itemDtoRequest) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Item item = ItemMapper.toItem(itemDtoRequest);
        item.setOwner(owner);
        Item savedItem = itemRepository.save(item);

        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDtoResponse updateItem(Long userId, Long itemId, ItemDtoUpdate itemDtoUpdate) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        Item item = itemRepository.findById(itemId)
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

        Item updatedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemOwnerDto getById(Long userId, Long itemId) {
        checkUserExists(userId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        List<CommentResponseDto> comments = commentRepository.findByItemIdOrderByCreatedAsc(itemId)
                .stream()
                .map(CommentMapper::commentResponseDto)
                .toList();

        ItemOwnerDto dto = new ItemOwnerDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        dto.setComments(comments);

        if (item.getOwner() != null && item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            Booking lastBooking = bookingRepository
                    .findFirstByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc(
                            itemId, Status.APPROVED, now
                    )
                    .orElse(null);

            Booking nextBooking = bookingRepository
                    .findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
                            itemId, Status.APPROVED, now
                    )
                    .orElse(null);

            dto.setLastBooking(lastBooking != null ? BookingMapper.toBook(lastBooking) : null);
            dto.setNextBooking(nextBooking != null ? BookingMapper.toBook(nextBooking) : null);
        }

        return dto;
    }

    @Override
    public List<ItemDtoResponse> getItemBySearch(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        return itemRepository.findItemBySearch(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public CommentResponseDto addComment(Long userId, Long itemId, CommentRequestDto commentRequestDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        LocalDateTime now = LocalDateTime.now();

        boolean hasCompletedBooking = bookingRepository.existsByItemIdAndBookerIdAndEndBeforeAndStatus(itemId, userId,
                now, Status.APPROVED);

        if (!hasCompletedBooking) {
            throw new ValidationException("Оставлять комментарий может только пользователь, завершивший аренду вещи");
        }

        Comment comment = new Comment();
        comment.setText(commentRequestDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(now);

        Comment savedComment = commentRepository.save(comment);

        return new CommentResponseDto(savedComment.getId(), savedComment.getText(),
                savedComment.getAuthor().getName(), savedComment.getCreated());
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }
}
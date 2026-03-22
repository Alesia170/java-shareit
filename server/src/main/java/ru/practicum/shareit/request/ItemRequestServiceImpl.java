package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemShortDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ItemRequestResponseDto saveNewRequest(Long userId, ItemRequestRequestDto itemRequestRequestDto) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestRequestDto);

        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());
        ItemRequest savedItemRequest = itemRequestRepository.save(itemRequest);

        return ItemRequestMapper.toResponseDto(savedItemRequest, List.of());
    }

    public List<ItemRequestResponseDto> getOwnRequests(Long userId) {
        checkUserExists(userId);

        return itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId)
                .stream()
                .map(itemRequest -> ItemRequestMapper.toResponseDto(itemRequest, List.of()))
                .toList();
    }

    public List<ItemRequestResponseDto> getRequestsCreatedByOtherUsers(Long userId) {
        checkUserExists(userId);

        return itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(userId)
                .stream()
                .map(itemRequest -> ItemRequestMapper.toResponseDto(itemRequest, List.of()))
                .toList();
    }

    public ItemRequestResponseDto getRequestById(Long userId, Long requestId) {
        checkUserExists(userId);
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id=" + requestId + " не найден"));

        List<ItemShortDto> items = itemRepository.findByRequestId(requestId)
                .stream()
                .map(ItemMapper::toShortDto)
                .toList();

        return ItemRequestMapper.toResponseDto(itemRequest, items);
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }
}

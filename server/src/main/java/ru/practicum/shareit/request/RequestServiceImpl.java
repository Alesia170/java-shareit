package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.request.dto.RequestMapper;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestResponseDto;
import ru.practicum.shareit.request.dto.ItemShortDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public RequestResponseDto saveNewRequest(Long userId, RequestDto requestDto) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Request request = RequestMapper.toItemRequest(requestDto);

        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        Request savedRequest = requestRepository.save(request);

        return RequestMapper.toResponseDto(savedRequest, List.of());
    }

    public List<RequestResponseDto> getOwnRequests(Long userId) {
        checkUserExists(userId);

        return requestRepository.findByRequestorIdOrderByCreatedDesc(userId)
                .stream()
                .map(itemRequest -> RequestMapper.toResponseDto(itemRequest, List.of()))
                .toList();
    }

    public List<RequestResponseDto> getRequestsCreatedByOtherUsers(Long userId) {
        checkUserExists(userId);

        return requestRepository.findByRequestorIdNotOrderByCreatedDesc(userId)
                .stream()
                .map(itemRequest -> RequestMapper.toResponseDto(itemRequest, List.of()))
                .toList();
    }

    public RequestResponseDto getRequestById(Long userId, Long requestId) {
        checkUserExists(userId);
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id=" + requestId + " не найден"));

        List<ItemShortDto> items = itemRepository.findByRequestId(requestId)
                .stream()
                .map(ItemMapper::toShortDto)
                .toList();

        return RequestMapper.toResponseDto(request, items);
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }
}

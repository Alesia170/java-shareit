package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestResponseDto saveNewRequest (Long userId, ItemRequestRequestDto itemRequestRequestDto);

    List<ItemRequestResponseDto> getOwnRequests(Long userId);

    List<ItemRequestResponseDto> getRequestsCreatedByOtherUsers(Long userId);

    ItemRequestResponseDto getRequestById(Long userId, Long requestId);
}

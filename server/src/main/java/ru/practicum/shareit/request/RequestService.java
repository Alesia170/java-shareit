package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestResponseDto;

import java.util.List;

public interface RequestService {

    RequestResponseDto saveNewRequest(Long userId, RequestDto requestDto);

    List<RequestResponseDto> getOwnRequests(Long userId);

    List<RequestResponseDto> getRequestsCreatedByOtherUsers(Long userId);

    RequestResponseDto getRequestById(Long userId, Long requestId);
}

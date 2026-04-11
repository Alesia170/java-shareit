package ru.practicum.shareit.request.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.request.Request;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestMapper {
    public static RequestResponseDto toResponseDto(Request request, List<ItemShortDto> items) {
        return new RequestResponseDto(request.getId(),
                request.getDescription(),
                request.getRequestor().getId(),
                request.getCreated(),
                items);
    }

    public static Request toItemRequest(RequestDto requestDto) {
        Request request = new Request();
        request.setDescription(requestDto.getDescription());
        return request;
    }
}

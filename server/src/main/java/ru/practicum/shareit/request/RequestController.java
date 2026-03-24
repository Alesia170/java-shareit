package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestResponseDto;

import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class RequestController {

    private final RequestService requestService;

    @PostMapping
    public RequestResponseDto saveNewRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestBody RequestDto requestDto) {
        return requestService.saveNewRequest(userId, requestDto);
    }

    @GetMapping
    public List<RequestResponseDto> getOwnRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestService.getOwnRequests(userId);
    }

    @GetMapping("/all")
    public List<RequestResponseDto> getRequestsCreatedByOtherUsers(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestService.getRequestsCreatedByOtherUsers(userId);
    }

    @GetMapping("/{requestId}")
    public RequestResponseDto getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable Long requestId) {
        return requestService.getRequestById(userId, requestId);
    }
}

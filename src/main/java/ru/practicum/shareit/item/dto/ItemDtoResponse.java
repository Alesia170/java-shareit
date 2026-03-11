package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.comment.CommentResponseDto;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDtoResponse {

    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private Long requestId;

    private List<CommentResponseDto> comments;
}

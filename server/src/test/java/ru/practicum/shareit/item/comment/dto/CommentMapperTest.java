package ru.practicum.shareit.item.comment.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.CommentResponseDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTest {

    @Test
    void shouldMapCommentToCommentResponseDto() {
        User author = new User();
        author.setId(2L);
        author.setName("Иван");

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Отличная вещь");
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.of(2026, 3, 23, 12, 30, 0));

        CommentResponseDto dto = CommentMapper.commentResponseDto(comment);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getText()).isEqualTo("Отличная вещь");
        assertThat(dto.getAuthorName()).isEqualTo("Иван");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2026, 3, 23, 12, 30, 0));
    }
}
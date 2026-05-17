package ru.yandex.practicum.service.comment;

import ru.yandex.practicum.dto.comment.CommentDto;
import ru.yandex.practicum.dto.comment.NewCommentDto;

import java.util.List;

public interface CommentService {

    List<CommentDto> getDeletedUserCommentsByAdmin(Long userId, int from, int size);

    List<CommentDto> getCommentsByEventPublic(Long eventId, int from, int size);

    CommentDto postCommentByUser(NewCommentDto dto, Long userId, Long eventId);

    CommentDto patсhCommentByUser(NewCommentDto dto, Long userId, Long commentId);

    void deleteCommentByAdmin(Long commentId);

    void deleteCommentByUser(Long commentId, Long userId);

}

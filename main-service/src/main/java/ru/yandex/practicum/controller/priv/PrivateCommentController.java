package ru.yandex.practicum.controller.priv;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.comment.CommentDto;
import ru.yandex.practicum.dto.comment.NewCommentDto;
import ru.yandex.practicum.service.comment.CommentService;

@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
@Validated
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto postComment(@RequestBody @Validated NewCommentDto dto,
                                  @PathVariable Long userId,
                                  @PathVariable Long eventId) {
        return commentService.postCommentByUser(dto, userId, eventId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto patchComment(@RequestBody @Validated NewCommentDto dto,
                                  @PathVariable Long userId,
                                  @PathVariable Long commentId) {
        return commentService.patсhCommentByUser(dto, userId, commentId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId,
                              @PathVariable Long userId) {
        commentService.deleteCommentByUser(commentId, userId);
    }

}

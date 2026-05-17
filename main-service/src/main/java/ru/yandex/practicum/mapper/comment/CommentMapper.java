package ru.yandex.practicum.mapper.comment;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.dto.comment.CommentDto;
import ru.yandex.practicum.dto.comment.NewCommentDto;
import ru.yandex.practicum.model.comment.Comment;
import ru.yandex.practicum.model.comment.CommentStatus;
import ru.yandex.practicum.model.event.Event;
import ru.yandex.practicum.model.user.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class CommentMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Comment toComment(NewCommentDto newCommentDto, User author, Event event) {
        return Comment.builder()
                .text(newCommentDto.getText())
                .author(author)
                .event(event)
                .status(CommentStatus.PUBLISHED)
                .published(LocalDateTime.now())
                .build();
    }

    public CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .authorId(comment.getAuthor().getId())
                .eventId(comment.getEvent().getId())
                .created(comment.getPublished())
                .updated(comment.getUpdated())
                .build();
    }

    public void updateCommentFromDto(NewCommentDto newCommentDto, Comment oldComment) {
        oldComment.setText(newCommentDto.getText());
        oldComment.setUpdated(LocalDateTime.now());
    }

}

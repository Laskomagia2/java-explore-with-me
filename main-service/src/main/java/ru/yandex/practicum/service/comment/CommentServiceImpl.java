package ru.yandex.practicum.service.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dal.comment.CommentRepository;
import ru.yandex.practicum.dal.event.EventRepository;
import ru.yandex.practicum.dal.user.UserRepository;
import ru.yandex.practicum.dto.comment.CommentDto;
import ru.yandex.practicum.dto.comment.NewCommentDto;
import ru.yandex.practicum.exception.NoPermissionException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.mapper.comment.CommentMapper;
import ru.yandex.practicum.model.comment.Comment;
import ru.yandex.practicum.model.comment.CommentStatus;
import ru.yandex.practicum.model.event.Event;
import ru.yandex.practicum.model.event.EventState;
import ru.yandex.practicum.model.user.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;

    @Override
    public List<CommentDto> getDeletedUserCommentsByAdmin(Long userId, int from, int size) {
        validateUser(userId);
        Pageable pageable = PageRequest.of(from / size, size);

        List<Comment> comments = commentRepository.findAllByAuthorIdAndStatus(userId, CommentStatus.DELETED_BY_ADMIN, pageable);

        return comments.stream().map(CommentMapper::toCommentDto).toList();
    }

    @Override
    public List<CommentDto> getCommentsByEventPublic(Long eventId, int from, int size) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Ивент с id " + eventId + " не найден");
        }
        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findAllByEventIdAndStatus(eventId, CommentStatus.PUBLISHED, pageable);

        return comments.stream().map(CommentMapper::toCommentDto).toList();
    }

    @Override
    @Transactional
    public CommentDto postCommentByUser(NewCommentDto dto, Long userId, Long eventId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        if (user.getCommentsBannedUntil() != null && user.getCommentsBannedUntil().isAfter(LocalDateTime.now())) {
            throw new NoPermissionException("Вы заблокированы за нарушение правил модерации. Бан истекает: "
                    + user.getCommentsBannedUntil().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Ивент с id " + eventId + " не найден"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NoPermissionException("Нельзя оставлять комментарии к неопубликованным событиям");
        }

        Comment comment = CommentMapper.toComment(dto, user, event);

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentDto patсhCommentByUser(NewCommentDto dto, Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException("Комментарий с Id " + commentId + " не найден"));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new NoPermissionException("Пользователь с id " + userId + " не является автором комментария " + commentId);
        }
        if (comment.getStatus() != CommentStatus.PUBLISHED) {
            throw new NotFoundException("Комментарий с Id " + commentId + " не найден");
        }
        CommentMapper.updateCommentFromDto(dto, comment);

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException("Комментарий с Id " + commentId + " не найден"));

        if (comment.getStatus() == CommentStatus.DELETED_BY_ADMIN) {
            return;
        }

        comment.setStatus(CommentStatus.DELETED_BY_ADMIN);
        commentRepository.save(comment);

        User author = comment.getAuthor();
        long infractionCount = commentRepository.countByAuthorIdAndStatus(author.getId(), CommentStatus.DELETED_BY_ADMIN);

        if (infractionCount >= 3) {
            author.setCommentsBannedUntil(LocalDateTime.now().plusHours(24));
            userRepository.save(author);
            log.debug("Пользователь {} получил бан до {}.", author.getId(), author.getCommentsBannedUntil());
        }
    }

    @Override
    @Transactional
    public void deleteCommentByUser(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException("Комментарий с Id " + commentId + " не найден"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new NoPermissionException("У пользователя " + userId + " нет доступа для удалени комментария " + commentId);
        }

        if (comment.getStatus() != CommentStatus.PUBLISHED) {
            throw new NotFoundException("Комментарий с Id " + commentId + " не найден.");
        }
        comment.setStatus(CommentStatus.DELETED_BY_USER);
        commentRepository.save(comment);
    }

    private void validateUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с Id: " + userId + " не найден");
        }
    }

}

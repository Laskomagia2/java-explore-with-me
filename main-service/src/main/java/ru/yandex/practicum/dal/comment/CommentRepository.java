package ru.yandex.practicum.dal.comment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.comment.Comment;
import ru.yandex.practicum.model.comment.CommentStatus;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByAuthorIdAndStatus(Long userId, CommentStatus status, Pageable pageable);

    Integer countByAuthorIdAndStatus(Long authorId, CommentStatus status);

    @Query("SELECT c FROM Comment c JOIN FETCH c.author JOIN FETCH c.event WHERE c.event.id = :eventId AND c.status = :status")
    List<Comment> findAllByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") CommentStatus status, Pageable pageable);

}

package ru.yandex.practicum.dal.user;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.user.User;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByIdIn(List<Long> ids, Pageable pageable);
}

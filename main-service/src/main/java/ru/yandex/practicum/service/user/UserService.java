package ru.yandex.practicum.service.user;

import ru.yandex.practicum.dto.user.NewUserRequest;
import ru.yandex.practicum.dto.user.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> getUsers(List<Long> ids, int from, int size);

    UserDto registerUser(NewUserRequest newUserRequest);

    void deleteUser(Long userId);

}

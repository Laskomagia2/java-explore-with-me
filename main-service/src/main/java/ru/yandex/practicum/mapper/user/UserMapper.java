package ru.yandex.practicum.mapper.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.user.NewUserRequest;
import ru.yandex.practicum.dto.user.UserDto;
import ru.yandex.practicum.dto.user.UserShortDto;
import ru.yandex.practicum.model.user.User;

@Component
public class UserMapper {

    public User toUser(NewUserRequest request) {
        if (request == null) return null;
        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .build();
    }

    public UserDto toUserDto(User user) {
        if (user == null) return null;
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public UserShortDto toUserShortDto(User user) {
        if (user == null) return null;
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

}

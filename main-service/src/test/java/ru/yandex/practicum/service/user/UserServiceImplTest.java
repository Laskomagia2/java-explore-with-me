package ru.yandex.practicum.service.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.dal.user.UserRepository;
import ru.yandex.practicum.dto.user.NewUserRequest;
import ru.yandex.practicum.dto.user.UserDto;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.user.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("user")
                .email("user@email.com")
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .name("user")
                .email("user@email.com")
                .build();
    }

    @Test
    void getUsersWithoutIdsTest() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(user)));

        List<UserDto> result = userService.getUsers(null, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("user");
    }

    @Test
    void getUsersWithIdsTest() {
        List<Long> ids = List.of(1L);
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAllByIdIn(ids, pageable)).thenReturn(List.of(user));

        List<UserDto> result = userService.getUsers(ids, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getUsersWithEmptyIdsTest() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(user)));

        List<UserDto> result = userService.getUsers(List.of(), 0, 10);

        assertThat(result).hasSize(1);
    }

    @Test
    void registerUserTest() {
        NewUserRequest request = NewUserRequest.builder()
                .name("user")
                .email("user@email.com")
                .build();

        when(userRepository.save(any())).thenReturn(user);

        UserDto result = userService.registerUser(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("user");
        assertThat(result.getEmail()).isEqualTo("user@email.com");
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void deleteUserTest() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUserNotFoundTest() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(NotFoundException.class);
    }

}

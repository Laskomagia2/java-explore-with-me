package ru.yandex.practicum.dto.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UserDtoTest {

    @Autowired
    JacksonTester<UserDto> json;

    @Test
    void userDtoSerializeTest() throws Exception {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("user")
                .email("user@email.com")
                .build();

        JsonContent<UserDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("user");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("user@email.com");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"id\": 1, \"name\": \"user\", \"email\": \"user@email.com\"}";

        UserDto result = json.parse(content).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("user");
        assertThat(result.getEmail()).isEqualTo("user@email.com");
    }

}

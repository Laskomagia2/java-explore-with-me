package ru.yandex.practicum.dto.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UserShortDtoTest {

    @Autowired
    JacksonTester<UserShortDto> json;

    @Test
    void userShortDtoSerializeTest() throws Exception {
        UserShortDto dto = UserShortDto.builder()
                .id(1L)
                .name("user")
                .build();

        JsonContent<UserShortDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("user");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"id\": 1, \"name\": \"user\"}";

        UserShortDto result = json.parse(content).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("user");
    }

}

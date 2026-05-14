package ru.yandex.practicum.dto.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class NewUserRequestTest {

    @Autowired
    JacksonTester<NewUserRequest> json;

    @Test
    void newUserRequestSerializeTest() throws Exception {
        NewUserRequest dto = NewUserRequest.builder()
                .name("user")
                .email("user@email.com")
                .build();

        JsonContent<NewUserRequest> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("user");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("user@email.com");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"name\": \"user\", \"email\": \"user@email.com\"}";

        NewUserRequest result = json.parse(content).getObject();

        assertThat(result.getName()).isEqualTo("user");
        assertThat(result.getEmail()).isEqualTo("user@email.com");
    }

}

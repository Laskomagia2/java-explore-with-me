package ru.yandex.practicum.dto.category;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class NewCategoryDtoTest {

    @Autowired
    JacksonTester<NewCategoryDto> json;

    @Test
    void newCategoryDtoTest() throws Exception {
        NewCategoryDto newCategoryDto = NewCategoryDto.builder()
                .name("category")
                .build();

        JsonContent<NewCategoryDto> result = json.write(newCategoryDto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("category");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"name\":\"category\"}";

        NewCategoryDto result = json.parse(content).getObject();

        assertThat(result.getName()).isEqualTo("category");
    }

}

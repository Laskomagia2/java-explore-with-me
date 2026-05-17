package ru.yandex.practicum.dto.category;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CategoryDtoTest {

    @Autowired
    JacksonTester<CategoryDto> json;

    @Test
    void categoryDtoTest() throws Exception {
        CategoryDto categoryDto = CategoryDto.builder()
                .id(1L)
                .name("category")
                .build();

        JsonContent<CategoryDto> result = json.write(categoryDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("category");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"name\":\"category\"}";

        CategoryDto result = json.parse(content).getObject();

        assertThat(result.getName()).isEqualTo("category");
    }

}

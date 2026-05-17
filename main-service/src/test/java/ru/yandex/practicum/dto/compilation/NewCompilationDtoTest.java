package ru.yandex.practicum.dto.compilation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class NewCompilationDtoTest {

    @Autowired
    JacksonTester<NewCompilationDto> json;

    @Test
    void newCompilationDtoTest() throws Exception {
        NewCompilationDto newCompilationDto = new NewCompilationDto();
        newCompilationDto.setTitle("title");
        newCompilationDto.setPinned(true);
        newCompilationDto.setEvents(List.of(1L));

        JsonContent<NewCompilationDto> result = json.write(newCompilationDto);

        assertThat(result).extractingJsonPathStringValue("$.title").isEqualTo("title");
        assertThat(result).extractingJsonPathBooleanValue("$.pinned").isEqualTo(true);
        assertThat(result).extractingJsonPathArrayValue("$.events").hasSize(1);
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\n" +
                "\"title\": \"title\",\n" +
                "\"pinned\": true,\n" +
                "\"events\": [1]}";

        NewCompilationDto result = json.parse(content).getObject();

        assertThat(result.getTitle()).isEqualTo("title");
        assertThat(result.getPinned()).isEqualTo(true);
        assertThat(result.getEvents().get(0)).isEqualTo(1L);
    }

}

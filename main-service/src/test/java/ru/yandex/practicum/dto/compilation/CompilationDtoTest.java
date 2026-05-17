package ru.yandex.practicum.dto.compilation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.yandex.practicum.dto.category.CategoryDto;
import ru.yandex.practicum.dto.event.EventShortDto;
import ru.yandex.practicum.dto.user.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CompilationDtoTest {

    @Autowired
    JacksonTester<CompilationDto> json;

    @Test
    void compilationDtoTest() throws Exception {

        EventShortDto eventShortDto = EventShortDto.builder()
                .id(1L)
                .annotation("annotation")
                .category(CategoryDto.builder().id(1L).name("category").build())
                .confirmedRequests(5L)
                .eventDate(String.valueOf(LocalDateTime.of(2026, 7, 20, 20, 0, 0)))
                .initiator(UserShortDto.builder().id(1L).name("name").build())
                .paid(true)
                .title("title")
                .views(100L)
                .build();

        CompilationDto compilationDto = CompilationDto.builder()
                .id(1L)
                .pinned(true)
                .title("title")
                .events(List.of(eventShortDto))
                .build();

        JsonContent<CompilationDto> result = json.write(compilationDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathBooleanValue("$.pinned").isEqualTo(true);
        assertThat(result).extractingJsonPathStringValue("$.title").isEqualTo("title");

        assertThat(result).extractingJsonPathArrayValue("$.events").hasSize(1);
        assertThat(result).extractingJsonPathNumberValue("$.events[0].id").isEqualTo(1);

        assertThat(result).extractingJsonPathStringValue("$.events[0].eventDate")
                .isEqualTo("2026-07-20T20:00");
    }

    @Test
    void testDeserialize() throws Exception {

        String content = "{\n" +
                "\"id\": 10,\n" +
                "\"pinned\": true,\n" +
                "\"title\":\"title\",\n" +
                "\"events\": [\n" +
                "{\n" +
                "\"id\": 1,\n" +
                "\"title\": \"title\",\n" +
                "\"eventDate\": \"2026-07-20 20:00:00\",\n" +
                "\"paid\": true,\n" +
                "\"views\": 100\n" +
                "}\n" +
                "]\n" +
                "}";

        CompilationDto result = json.parse(content).getObject();

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getTitle()).isEqualTo("title");
        assertThat(result.getPinned()).isEqualTo(true);

        assertThat(result.getEvents()).hasSize(1);
        EventShortDto event = result.getEvents().get(0);

        assertThat(event.getId()).isEqualTo(1L);
        assertThat(event.getTitle()).isEqualTo("title");

    }
}

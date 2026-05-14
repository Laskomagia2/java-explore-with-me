package ru.yandex.practicum.dto.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.yandex.practicum.dto.category.CategoryDto;
import ru.yandex.practicum.dto.user.UserShortDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class EventShortDtoTest {

    @Autowired
    JacksonTester<EventShortDto> json;

    @Test
    void eventShortDtoSerializeTest() throws Exception {
        EventShortDto dto = EventShortDto.builder()
                .id(1L)
                .annotation("annotation")
                .category(CategoryDto.builder().id(1L).name("category").build())
                .confirmedRequests(5L)
                .eventDate("2026-08-20 20:00:00")
                .initiator(UserShortDto.builder().id(1L).name("user").build())
                .paid(true)
                .title("title")
                .views(100L)
                .build();

        JsonContent<EventShortDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.annotation").isEqualTo("annotation");
        assertThat(result).extractingJsonPathNumberValue("$.category.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.confirmedRequests").isEqualTo(5);
        assertThat(result).extractingJsonPathStringValue("$.eventDate").isEqualTo("2026-08-20 20:00:00");
        assertThat(result).extractingJsonPathBooleanValue("$.paid").isEqualTo(true);
        assertThat(result).extractingJsonPathStringValue("$.title").isEqualTo("title");
        assertThat(result).extractingJsonPathNumberValue("$.views").isEqualTo(100);
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{" +
                "\"id\": 1," +
                "\"annotation\": \"annotation\"," +
                "\"eventDate\": \"2026-08-20 20:00:00\"," +
                "\"paid\": true," +
                "\"title\": \"title\"," +
                "\"views\": 100}";

        EventShortDto result = json.parse(content).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAnnotation()).isEqualTo("annotation");
        assertThat(result.getTitle()).isEqualTo("title");
        assertThat(result.getViews()).isEqualTo(100L);
    }

}

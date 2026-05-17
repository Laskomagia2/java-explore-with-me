package ru.yandex.practicum.dto.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.yandex.practicum.dto.LocationDto;
import ru.yandex.practicum.dto.category.CategoryDto;
import ru.yandex.practicum.dto.user.UserShortDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class EventFullDtoTest {

    @Autowired
    JacksonTester<EventFullDto> json;

    @Test
    void eventFullDtoSerializeTest() throws Exception {
        EventFullDto dto = EventFullDto.builder()
                .id(1L)
                .annotation("annotation")
                .category(CategoryDto.builder().id(1L).name("category").build())
                .confirmedRequests(5L)
                .createdOn("2026-07-20 20:00:00")
                .description("description")
                .eventDate("2026-08-20 20:00:00")
                .initiator(UserShortDto.builder().id(1L).name("user").build())
                .location(LocationDto.builder().lat(55.7f).lon(37.6f).build())
                .paid(true)
                .participantLimit(10)
                .publishedOn("2026-07-21 20:00:00")
                .requestModeration(true)
                .state("PUBLISHED")
                .title("title")
                .views(100L)
                .build();

        JsonContent<EventFullDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.annotation").isEqualTo("annotation");
        assertThat(result).extractingJsonPathNumberValue("$.category.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.category.name").isEqualTo("category");
        assertThat(result).extractingJsonPathNumberValue("$.confirmedRequests").isEqualTo(5);
        assertThat(result).extractingJsonPathStringValue("$.createdOn").isEqualTo("2026-07-20 20:00:00");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("description");
        assertThat(result).extractingJsonPathStringValue("$.eventDate").isEqualTo("2026-08-20 20:00:00");
        assertThat(result).extractingJsonPathNumberValue("$.initiator.id").isEqualTo(1);
        assertThat(result).extractingJsonPathBooleanValue("$.paid").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.participantLimit").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.state").isEqualTo("PUBLISHED");
        assertThat(result).extractingJsonPathStringValue("$.title").isEqualTo("title");
        assertThat(result).extractingJsonPathNumberValue("$.views").isEqualTo(100);
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{" +
                "\"id\": 1," +
                "\"annotation\": \"annotation\"," +
                "\"description\": \"description\"," +
                "\"eventDate\": \"2026-08-20 20:00:00\"," +
                "\"paid\": true," +
                "\"participantLimit\": 10," +
                "\"state\": \"PUBLISHED\"," +
                "\"title\": \"title\"," +
                "\"views\": 100}";

        EventFullDto result = json.parse(content).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAnnotation()).isEqualTo("annotation");
        assertThat(result.getTitle()).isEqualTo("title");
        assertThat(result.getState()).isEqualTo("PUBLISHED");
        assertThat(result.getViews()).isEqualTo(100L);
    }

}

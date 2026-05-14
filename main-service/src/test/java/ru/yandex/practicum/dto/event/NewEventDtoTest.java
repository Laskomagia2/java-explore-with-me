package ru.yandex.practicum.dto.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.yandex.practicum.dto.LocationDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class NewEventDtoTest {

    @Autowired
    JacksonTester<NewEventDto> json;

    @Test
    void newEventDtoSerializeTest() throws Exception {
        NewEventDto dto = NewEventDto.builder()
                .annotation("annotation for new event")
                .category(1L)
                .description("description for new event")
                .eventDate("2026-08-20 20:00:00")
                .location(LocationDto.builder().lat(55.7f).lon(37.6f).build())
                .paid(true)
                .participantLimit(10)
                .requestModeration(true)
                .title("title")
                .build();

        JsonContent<NewEventDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.annotation").isEqualTo("annotation for new event");
        assertThat(result).extractingJsonPathNumberValue("$.category").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("description for new event");
        assertThat(result).extractingJsonPathStringValue("$.eventDate").isEqualTo("2026-08-20 20:00:00");
        assertThat(result).extractingJsonPathBooleanValue("$.paid").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.participantLimit").isEqualTo(10);
        assertThat(result).extractingJsonPathBooleanValue("$.requestModeration").isEqualTo(true);
        assertThat(result).extractingJsonPathStringValue("$.title").isEqualTo("title");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{" +
                "\"annotation\": \"annotation for new event\"," +
                "\"category\": 1," +
                "\"description\": \"description for new event\"," +
                "\"eventDate\": \"2026-08-20 20:00:00\"," +
                "\"location\": {\"lat\": 55.7, \"lon\": 37.6}," +
                "\"paid\": true," +
                "\"participantLimit\": 10," +
                "\"requestModeration\": true," +
                "\"title\": \"title\"}";

        NewEventDto result = json.parse(content).getObject();

        assertThat(result.getAnnotation()).isEqualTo("annotation for new event");
        assertThat(result.getCategory()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("title");
        assertThat(result.getPaid()).isEqualTo(true);
    }

}

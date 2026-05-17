package ru.yandex.practicum.dto.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.yandex.practicum.dto.LocationDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UpdateEventUserRequestTest {

    @Autowired
    JacksonTester<UpdateEventUserRequest> json;

    @Test
    void updateEventUserRequestSerializeTest() throws Exception {
        UpdateEventUserRequest dto = UpdateEventUserRequest.builder()
                .annotation("updated annotation value")
                .category(2L)
                .description("updated description value")
                .eventDate("2026-09-20 20:00:00")
                .location(LocationDto.builder().lat(55.7f).lon(37.6f).build())
                .paid(false)
                .participantLimit(20)
                .requestModeration(false)
                .stateAction(StateActionUser.SEND_TO_REVIEW)
                .title("updated title")
                .build();

        JsonContent<UpdateEventUserRequest> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.annotation").isEqualTo("updated annotation value");
        assertThat(result).extractingJsonPathNumberValue("$.category").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.eventDate").isEqualTo("2026-09-20 20:00:00");
        assertThat(result).extractingJsonPathStringValue("$.stateAction").isEqualTo("SEND_TO_REVIEW");
        assertThat(result).extractingJsonPathStringValue("$.title").isEqualTo("updated title");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{" +
                "\"annotation\": \"updated annotation value\"," +
                "\"category\": 2," +
                "\"stateAction\": \"CANCEL_REVIEW\"," +
                "\"title\": \"updated title\"}";

        UpdateEventUserRequest result = json.parse(content).getObject();

        assertThat(result.getAnnotation()).isEqualTo("updated annotation value");
        assertThat(result.getCategory()).isEqualTo(2L);
        assertThat(result.getStateAction()).isEqualTo(StateActionUser.CANCEL_REVIEW);
        assertThat(result.getTitle()).isEqualTo("updated title");
    }

}

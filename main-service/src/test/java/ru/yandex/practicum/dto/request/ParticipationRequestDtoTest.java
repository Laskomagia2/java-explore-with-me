package ru.yandex.practicum.dto.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ParticipationRequestDtoTest {

    @Autowired
    JacksonTester<ParticipationRequestDto> json;

    @Test
    void participationRequestDtoSerializeTest() throws Exception {
        ParticipationRequestDto dto = ParticipationRequestDto.builder()
                .id(1L)
                .created("2026-07-20 20:00:00")
                .event(1L)
                .requester(2L)
                .status("PENDING")
                .build();

        JsonContent<ParticipationRequestDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2026-07-20 20:00:00");
        assertThat(result).extractingJsonPathNumberValue("$.event").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.requester").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("PENDING");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"id\": 1, \"created\": \"2026-07-20 20:00:00\", " +
                "\"event\": 1, \"requester\": 2, \"status\": \"CONFIRMED\"}";

        ParticipationRequestDto result = json.parse(content).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCreated()).isEqualTo("2026-07-20 20:00:00");
        assertThat(result.getEvent()).isEqualTo(1L);
        assertThat(result.getRequester()).isEqualTo(2L);
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
    }

}

package ru.yandex.practicum.dto.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class EventRequestStatusUpdateResultTest {

    @Autowired
    JacksonTester<EventRequestStatusUpdateResult> json;

    @Test
    void eventRequestStatusUpdateResultSerializeTest() throws Exception {
        ParticipationRequestDto confirmed = ParticipationRequestDto.builder()
                .id(1L)
                .created("2026-07-20 20:00:00")
                .event(1L)
                .requester(2L)
                .status("CONFIRMED")
                .build();

        ParticipationRequestDto rejected = ParticipationRequestDto.builder()
                .id(2L)
                .created("2026-07-20 20:00:00")
                .event(1L)
                .requester(3L)
                .status("REJECTED")
                .build();

        EventRequestStatusUpdateResult dto = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(List.of(confirmed))
                .rejectedRequests(List.of(rejected))
                .build();

        JsonContent<EventRequestStatusUpdateResult> result = json.write(dto);

        assertThat(result).extractingJsonPathArrayValue("$.confirmedRequests").hasSize(1);
        assertThat(result).extractingJsonPathArrayValue("$.rejectedRequests").hasSize(1);
        assertThat(result).extractingJsonPathStringValue("$.confirmedRequests[0].status").isEqualTo("CONFIRMED");
        assertThat(result).extractingJsonPathStringValue("$.rejectedRequests[0].status").isEqualTo("REJECTED");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{" +
                "\"confirmedRequests\": [{\"id\": 1, \"status\": \"CONFIRMED\"}]," +
                "\"rejectedRequests\": [{\"id\": 2, \"status\": \"REJECTED\"}]}";

        EventRequestStatusUpdateResult result = json.parse(content).getObject();

        assertThat(result.getConfirmedRequests()).hasSize(1);
        assertThat(result.getRejectedRequests()).hasSize(1);
        assertThat(result.getConfirmedRequests().get(0).getStatus()).isEqualTo("CONFIRMED");
    }

}

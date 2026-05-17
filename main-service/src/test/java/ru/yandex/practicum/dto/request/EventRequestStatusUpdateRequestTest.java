package ru.yandex.practicum.dto.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class EventRequestStatusUpdateRequestTest {

    @Autowired
    JacksonTester<EventRequestStatusUpdateRequest> json;

    @Test
    void eventRequestStatusUpdateRequestSerializeTest() throws Exception {
        EventRequestStatusUpdateRequest dto = new EventRequestStatusUpdateRequest();
        dto.setRequestIds(List.of(1L, 2L, 3L));
        dto.setStatus("CONFIRMED");

        JsonContent<EventRequestStatusUpdateRequest> result = json.write(dto);

        assertThat(result).extractingJsonPathArrayValue("$.requestIds").hasSize(3);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("CONFIRMED");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"requestIds\": [1, 2, 3], \"status\": \"REJECTED\"}";

        EventRequestStatusUpdateRequest result = json.parse(content).getObject();

        assertThat(result.getRequestIds()).hasSize(3);
        assertThat(result.getRequestIds().get(0)).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo("REJECTED");
    }

}

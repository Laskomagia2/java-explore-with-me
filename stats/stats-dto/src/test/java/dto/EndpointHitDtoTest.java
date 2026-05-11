package dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.yandex.practicum.dto.EndpointHitDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EndpointHitDtoTest {

    private JacksonTester<EndpointHitDto> json;

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        JacksonTester.initFields(this, objectMapper);
    }

    @Test
    void testEndpointHitDtoSerialization() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 5, 11, 19, 0, 0);
        EndpointHitDto dto = EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.163.0.1")
                .timestamp(now)
                .build();

        JsonContent<EndpointHitDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.app").isEqualTo("ewm-main-service");
        assertThat(result).extractingJsonPathStringValue("$.uri").isEqualTo("/events/1");
        assertThat(result).extractingJsonPathStringValue("$.ip").isEqualTo("192.163.0.1");
        assertThat(result).extractingJsonPathStringValue("$.timestamp").isEqualTo("2026-05-11 19:00:00");
    }

    @Test
    void testEndpointHitDtoDeserialization() throws Exception {
        String content = "{\"app\":\"ewm-main-service\",\"uri\":\"/events/1\",\"ip\":\"192.163.0.1\",\"timestamp\":\"2026-05-11 19:00:00\"}";

        EndpointHitDto result = json.parse(content).getObject();

        assertThat(result.getApp()).isEqualTo("ewm-main-service");
        assertThat(result.getTimestamp()).isEqualTo(LocalDateTime.of(2026, 5, 11, 19, 0, 0));
    }
}
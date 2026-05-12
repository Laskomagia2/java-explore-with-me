package dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.yandex.practicum.dto.ViewStatsDto;

import static org.assertj.core.api.Assertions.assertThat;

class ViewStatsDtoTest {

    private JacksonTester<ViewStatsDto> json;

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        JacksonTester.initFields(this, objectMapper);
    }

    @Test
    void testViewStatsDtoSerialization() throws Exception {
        ViewStatsDto dto = new ViewStatsDto("ewm-main-service", "/events/1", 10L);

        JsonContent<ViewStatsDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.app").isEqualTo("ewm-main-service");
        assertThat(result).extractingJsonPathStringValue("$.uri").isEqualTo("/events/1");
        assertThat(result).extractingJsonPathNumberValue("$.hits").isEqualTo(10);
    }

    @Test
    void testViewStatsDtoDeserialization() throws Exception {
        String content = "{\"app\":\"ewm-main-service\",\"uri\":\"/events/1\",\"hits\":\"10\"}";

        ViewStatsDto result = json.parse(content).getObject();

        assertThat(result.getApp()).isEqualTo("ewm-main-service");
        assertThat(result.getHits()).isEqualTo(10L);
        assertThat(result.getUri()).isEqualTo("/events/1");
    }
}

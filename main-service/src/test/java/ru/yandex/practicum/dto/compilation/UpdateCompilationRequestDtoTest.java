package ru.yandex.practicum.dto.compilation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UpdateCompilationRequestDtoTest {

    @Autowired
    JacksonTester<UpdateCompilationRequest> json;

    @Test
    void updateCompilationRequestDtoTest() throws Exception {
        UpdateCompilationRequest updateCompilationRequest = new UpdateCompilationRequest();
        updateCompilationRequest.setTitle("title");
        updateCompilationRequest.setPinned(true);
        updateCompilationRequest.setEvents(List.of(1L));

        JsonContent<UpdateCompilationRequest> result = json.write(updateCompilationRequest);

        assertThat(result).extractingJsonPathStringValue("$.title").isEqualTo("title");
        assertThat(result).extractingJsonPathBooleanValue("$.pinned").isEqualTo(true);
        assertThat(result).extractingJsonPathArrayValue("$.events").hasSize(1);
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\n" +
                "\"title\": \"title\",\n" +
                "\"pinned\": true,\n" +
                "\"events\": [1]}";

        UpdateCompilationRequest result = json.parse(content).getObject();

        assertThat(result.getTitle()).isEqualTo("title");
        assertThat(result.getPinned()).isEqualTo(true);
        assertThat(result.getEvents().get(0)).isEqualTo(1L);
    }

}

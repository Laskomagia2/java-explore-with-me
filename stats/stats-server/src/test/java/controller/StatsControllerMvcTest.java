package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.ErrorHandler;
import ru.yandex.practicum.StatsServerApplication;
import ru.yandex.practicum.controller.StatsController;
import ru.yandex.practicum.dto.EndpointHitDto;
import ru.yandex.practicum.dto.ViewStatsDto;
import ru.yandex.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = StatsController.class)
@ContextConfiguration(classes = StatsServerApplication.class)
@Import(ErrorHandler.class)
public class StatsControllerMvcTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private StatsService statsService;

    @Test
    void postHitReturnOkTest() throws Exception {
        EndpointHitDto dto = EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.163.0.1")
                .timestamp(LocalDateTime.now())
                .build();
        mvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        verify(statsService).saveHit(any(EndpointHitDto.class));
    }

    @Test
    void getStatsReturnStatsListTest() throws Exception {
        ViewStatsDto responseDto = new ViewStatsDto("ewm-main-service", "/events/1", 1L);

        when(statsService.getStats(any(), any(), any(), anyBoolean()))
                .thenReturn(List.of(responseDto));

        mvc.perform(get("/stats")
                        .param("start", "2020-01-01 00:00:00")
                        .param("end", "2035-01-01 00:00:00")
                        .param("uris", "/events/1")
                        .param("unique", "false")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].app").value("ewm-main-service"))
                .andExpect(jsonPath("$[0].hits").value(1));
    }

    @Test
    void getStatsReturnBadRequestTest() throws Exception {
        mvc.perform(get("/stats"))
                .andExpect(status().isBadRequest());
    }

}

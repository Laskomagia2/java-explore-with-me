package ru.yandex.practicum.controller.priv;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.dto.request.ParticipationRequestDto;
import ru.yandex.practicum.service.request.RequestService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ParticipationRequestController.class)
public class ParticipationRequestControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private RequestService requestService;

    private ParticipationRequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestDto = ParticipationRequestDto.builder()
                .id(1L)
                .created("2026-07-20 20:00:00")
                .event(1L)
                .requester(2L)
                .status("PENDING")
                .build();
    }

    @Test
    void getRequestsTest() throws Exception {
        when(requestService.getRequests(anyLong())).thenReturn(List.of(requestDto));

        mvc.perform(get("/users/2/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(requestDto.getId().intValue())))
                .andExpect(jsonPath("$[0].status", is(requestDto.getStatus())));
    }

    @Test
    void createRequestTest() throws Exception {
        when(requestService.createRequest(anyLong(), anyLong())).thenReturn(requestDto);

        mvc.perform(post("/users/2/requests")
                        .param("eventId", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(requestDto.getId().intValue())))
                .andExpect(jsonPath("$.event", is(requestDto.getEvent().intValue())))
                .andExpect(jsonPath("$.requester", is(requestDto.getRequester().intValue())))
                .andExpect(jsonPath("$.status", is(requestDto.getStatus())));
    }

    @Test
    void cancelRequestTest() throws Exception {
        ParticipationRequestDto canceledDto = ParticipationRequestDto.builder()
                .id(1L)
                .created("2026-07-20 20:00:00")
                .event(1L)
                .requester(2L)
                .status("CANCELED")
                .build();

        when(requestService.cancelRequest(anyLong(), anyLong())).thenReturn(canceledDto);

        mvc.perform(patch("/users/2/requests/1/cancel")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(canceledDto.getId().intValue())))
                .andExpect(jsonPath("$.status", is("CANCELED")));
    }

}

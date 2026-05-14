package ru.yandex.practicum.controller.priv;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.dto.LocationDto;
import ru.yandex.practicum.dto.category.CategoryDto;
import ru.yandex.practicum.dto.event.*;
import ru.yandex.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.yandex.practicum.dto.request.ParticipationRequestDto;
import ru.yandex.practicum.dto.user.UserShortDto;
import ru.yandex.practicum.service.event.EventService;
import ru.yandex.practicum.service.request.RequestService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PrivateEventController.class)
public class PrivateEventControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private RequestService requestService;

    private EventFullDto eventFullDto;
    private EventShortDto eventShortDto;

    @BeforeEach
    void setUp() {
        eventFullDto = EventFullDto.builder()
                .id(1L)
                .annotation("annotation for testing event")
                .category(CategoryDto.builder().id(1L).name("category").build())
                .confirmedRequests(0L)
                .createdOn("2026-07-20 20:00:00")
                .description("description for testing event")
                .eventDate("2026-08-20 20:00:00")
                .initiator(UserShortDto.builder().id(1L).name("user").build())
                .location(LocationDto.builder().lat(55.7f).lon(37.6f).build())
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .state("PENDING")
                .title("title")
                .views(0L)
                .build();

        eventShortDto = EventShortDto.builder()
                .id(1L)
                .annotation("annotation for testing event")
                .category(CategoryDto.builder().id(1L).name("category").build())
                .confirmedRequests(0L)
                .eventDate("2026-08-20 20:00:00")
                .initiator(UserShortDto.builder().id(1L).name("user").build())
                .paid(false)
                .title("title")
                .views(0L)
                .build();
    }

    @Test
    void getEventsByUserTest() throws Exception {
        when(eventService.getEventsByUser(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(eventShortDto));

        mvc.perform(get("/users/1/events")
                        .param("from", "0")
                        .param("size", "10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(eventShortDto.getId().intValue())))
                .andExpect(jsonPath("$[0].title", is(eventShortDto.getTitle())));
    }

    @Test
    void addEventTest() throws Exception {
        NewEventDto newEventDto = NewEventDto.builder()
                .annotation("annotation for testing event")
                .category(1L)
                .description("description for testing event")
                .eventDate("2026-08-20 20:00:00")
                .location(LocationDto.builder().lat(55.7f).lon(37.6f).build())
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .title("title")
                .build();

        when(eventService.addEvent(anyLong(), any())).thenReturn(eventFullDto);

        mvc.perform(post("/users/1/events")
                        .content(objectMapper.writeValueAsString(newEventDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId().intValue())))
                .andExpect(jsonPath("$.title", is(eventFullDto.getTitle())))
                .andExpect(jsonPath("$.state", is("PENDING")));
    }

    @Test
    void getEventByUserAndIdTest() throws Exception {
        when(eventService.getEventByUserAndId(anyLong(), anyLong())).thenReturn(eventFullDto);

        mvc.perform(get("/users/1/events/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId().intValue())))
                .andExpect(jsonPath("$.title", is(eventFullDto.getTitle())));
    }

    @Test
    void updateEventByUserTest() throws Exception {
        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .title("updated title")
                .stateAction(StateActionUser.SEND_TO_REVIEW)
                .build();

        when(eventService.updateEventByUser(anyLong(), anyLong(), any())).thenReturn(eventFullDto);

        mvc.perform(patch("/users/1/events/1")
                        .content(objectMapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId().intValue())));
    }

    @Test
    void getEventRequestsTest() throws Exception {
        ParticipationRequestDto requestDto = ParticipationRequestDto.builder()
                .id(1L)
                .created("2026-07-20 20:00:00")
                .event(1L)
                .requester(2L)
                .status("PENDING")
                .build();

        when(requestService.getEventRequests(anyLong(), anyLong())).thenReturn(List.of(requestDto));

        mvc.perform(get("/users/1/events/1/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(requestDto.getId().intValue())));
    }

    @Test
    void updateRequestStatusTest() throws Exception {
        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(1L));
        updateRequest.setStatus("CONFIRMED");

        ParticipationRequestDto confirmed = ParticipationRequestDto.builder()
                .id(1L)
                .created("2026-07-20 20:00:00")
                .event(1L)
                .requester(2L)
                .status("CONFIRMED")
                .build();

        EventRequestStatusUpdateResult result = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(List.of(confirmed))
                .rejectedRequests(List.of())
                .build();

        when(requestService.updateRequestStatus(anyLong(), anyLong(), any())).thenReturn(result);

        mvc.perform(patch("/users/1/events/1/requests")
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedRequests", hasSize(1)))
                .andExpect(jsonPath("$.rejectedRequests", hasSize(0)));
    }

}

package ru.yandex.practicum.controller.admin;

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
import ru.yandex.practicum.dto.event.EventFullDto;
import ru.yandex.practicum.dto.event.StateActionAdmin;
import ru.yandex.practicum.dto.event.UpdateEventAdminRequest;
import ru.yandex.practicum.dto.user.UserShortDto;
import ru.yandex.practicum.service.event.EventService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminEventController.class)
public class AdminEventControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private EventService eventService;

    private EventFullDto eventFullDto;

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
    }

    @Test
    void updateEventByAdminTest() throws Exception {
        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .stateAction(StateActionAdmin.PUBLISH_EVENT)
                .build();

        EventFullDto published = EventFullDto.builder()
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
                .state("PUBLISHED")
                .title("title")
                .views(0L)
                .build();

        when(eventService.updateEventByAdmin(anyLong(), any())).thenReturn(published);

        mvc.perform(patch("/admin/events/1")
                        .content(objectMapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(published.getId().intValue())))
                .andExpect(jsonPath("$.state", is("PUBLISHED")));
    }

    @Test
    void getEventsByAdminTest() throws Exception {
        when(eventService.getEventsByAdmin(any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(eventFullDto));

        mvc.perform(get("/admin/events")
                        .param("from", "0")
                        .param("size", "10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(eventFullDto.getId().intValue())))
                .andExpect(jsonPath("$[0].title", is(eventFullDto.getTitle())));
    }

}

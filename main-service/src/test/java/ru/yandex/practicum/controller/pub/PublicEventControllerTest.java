package ru.yandex.practicum.controller.pub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.client.StatsClient;
import ru.yandex.practicum.dto.LocationDto;
import ru.yandex.practicum.dto.category.CategoryDto;
import ru.yandex.practicum.dto.event.EventFullDto;
import ru.yandex.practicum.dto.event.EventShortDto;
import ru.yandex.practicum.dto.user.UserShortDto;
import ru.yandex.practicum.service.event.EventService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicEventController.class)
public class PublicEventControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private StatsClient statsClient;

    private EventShortDto eventShortDto;
    private EventFullDto eventFullDto;

    @BeforeEach
    void setUp() {
        eventShortDto = EventShortDto.builder()
                .id(1L)
                .annotation("annotation for testing event")
                .category(CategoryDto.builder().id(1L).name("category").build())
                .confirmedRequests(0L)
                .eventDate("2026-08-20 20:00:00")
                .initiator(UserShortDto.builder().id(1L).name("user").build())
                .paid(false)
                .title("title")
                .views(100L)
                .build();

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
                .state("PUBLISHED")
                .title("title")
                .views(100L)
                .build();
    }

    @Test
    void getEventsTest() throws Exception {
        doNothing().when(statsClient).saveHit(any());
        when(eventService.getEventsPublic(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(eventShortDto));

        mvc.perform(get("/events")
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
    void getEventTest() throws Exception {
        doNothing().when(statsClient).saveHit(any());
        when(eventService.getEventPublicById(anyLong())).thenReturn(eventFullDto);

        mvc.perform(get("/events/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId().intValue())))
                .andExpect(jsonPath("$.title", is(eventFullDto.getTitle())))
                .andExpect(jsonPath("$.state", is("PUBLISHED")))
                .andExpect(jsonPath("$.views", is(eventFullDto.getViews().intValue())));
    }

}

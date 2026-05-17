package ru.yandex.practicum.controller.pub;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.client.StatsClient;
import ru.yandex.practicum.dto.EndpointHitDto;
import ru.yandex.practicum.dto.event.EventFullDto;
import ru.yandex.practicum.dto.event.EventShortDto;
import ru.yandex.practicum.service.event.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PublicEventController {

    private final EventService eventService;
    private final StatsClient statsClient;

    private static final String APP_NAME = "ewm-main-service";

    @GetMapping
    public List<EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size,
            HttpServletRequest request) {

        log.info("Публичный поиск событий: text={}, categories={}, paid={}", text, categories, paid);

        statsClient.saveHit(EndpointHitDto.builder()
                        .app(APP_NAME)
                        .uri(request.getRequestURI())
                        .ip(request.getRemoteAddr())
                        .timestamp(LocalDateTime.now())
                        .build());

        return eventService.getEventsPublic(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
    }

    @GetMapping("/{id}")
    public EventFullDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        log.info("Публичный просмотр события id={}", id);

        statsClient.saveHit(EndpointHitDto.builder()
                .app(APP_NAME)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());

        return eventService.getEventPublicById(id);
    }
}

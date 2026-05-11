package ru.yandex.practicum.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.yandex.practicum.dto.EndpointHitDto;
import ru.yandex.practicum.dto.ViewStatsDto;

import java.util.List;
import java.util.Map;

@Service
public class StatsClient {
    private final RestTemplate rest;

    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        this.rest = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
    }

    public void saveHit(EndpointHitDto hitDto) {
        rest.postForEntity("/hit", hitDto, Void.class);
    }

    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, Boolean unique) {
        Map<String, Object> parameters = Map.of(
                "start", start,
                "end", end,
                "uris", uris != null ? String.join(",", uris) : "",
                "unique", unique
        );

        ViewStatsDto[] response = rest.getForObject(
                "/stats?start={start}&end={end}&uris={uris}&unique={unique}",
                ViewStatsDto[].class,
                parameters
        );

        return response != null ? List.of(response) : List.of();
    }
}

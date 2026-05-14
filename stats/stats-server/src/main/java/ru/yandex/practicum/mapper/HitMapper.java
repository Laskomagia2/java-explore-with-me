package ru.yandex.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.dto.EndpointHitDto;
import ru.yandex.practicum.model.Hit;

@UtilityClass
public class HitMapper {
    public Hit toHit(EndpointHitDto dto) {
        return Hit.builder()
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }
}

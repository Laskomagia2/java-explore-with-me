package ru.yandex.practicum.mapper.request;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.request.ParticipationRequestDto;
import ru.yandex.practicum.model.request.Request;

import java.time.format.DateTimeFormatter;

@Component
public class RequestMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ParticipationRequestDto toDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated().format(FORMATTER))
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus().name())
                .build();
    }
}

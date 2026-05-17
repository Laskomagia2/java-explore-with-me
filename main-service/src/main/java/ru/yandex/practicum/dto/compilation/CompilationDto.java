package ru.yandex.practicum.dto.compilation;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.dto.event.EventShortDto;

import java.util.List;

@Data
@Builder
public class CompilationDto {
    private Long id;
    private List<EventShortDto> events;
    private Boolean pinned;
    private String title;
}

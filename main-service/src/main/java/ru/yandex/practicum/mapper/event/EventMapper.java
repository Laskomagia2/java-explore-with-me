package ru.yandex.practicum.mapper.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.event.*;
import ru.yandex.practicum.dto.LocationDto;
import ru.yandex.practicum.mapper.category.CategoryMapper;
import ru.yandex.practicum.mapper.user.UserMapper;
import ru.yandex.practicum.model.category.Category;
import ru.yandex.practicum.model.event.Event;
import ru.yandex.practicum.model.event.EventState;
import ru.yandex.practicum.model.Location;
import ru.yandex.practicum.model.user.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class EventMapper {

    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Event toEvent(NewEventDto dto, User initiator, Category category) {
        if (dto == null) return null;

        return Event.builder()
                .annotation(dto.getAnnotation())
                .category(category)
                .description(dto.getDescription())
                .eventDate(LocalDateTime.parse(dto.getEventDate(), FORMATTER))
                .initiator(initiator)
                .location(new Location(dto.getLocation().getLat(), dto.getLocation().getLon()))
                .paid(dto.getPaid())
                .participantLimit(dto.getParticipantLimit())
                .requestModeration(dto.getRequestModeration())
                .title(dto.getTitle())
                .state(EventState.PENDING)
                .createdOn(LocalDateTime.now())
                .build();
    }

    public EventFullDto toEventFullDto(Event event, Long confirmedRequests, Long views) {
        if (event == null) return null;

        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .createdOn(event.getCreatedOn().format(FORMATTER))
                .description(event.getDescription())
                .eventDate(event.getEventDate().format(FORMATTER))
                .initiator(userMapper.toUserShortDto(event.getInitiator()))
                .location(new LocationDto(event.getLocation().getLat(), event.getLocation().getLon()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn() != null ? event.getPublishedOn().format(FORMATTER) : null)
                .requestModeration(event.getRequestModeration())
                .state(event.getState().name())
                .title(event.getTitle())
                .views(views)
                .build();
    }

    public EventShortDto toEventShortDto(Event event, Long confirmedRequests, Long views) {
        if (event == null) return null;

        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .eventDate(event.getEventDate().format(FORMATTER))
                .initiator(userMapper.toUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(views)
                .build();
    }

    public void updateEventFromDto(UpdateEventUserRequest dto, Event event, Category category) {
        if (dto == null) return;

        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (category != null) event.setCategory(category);
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getEventDate() != null) {
            event.setEventDate(LocalDateTime.parse(dto.getEventDate(), FORMATTER));
        }
        if (dto.getLocation() != null) {
            event.setLocation(new Location(dto.getLocation().getLat(), dto.getLocation().getLon()));
        }
        if (dto.getPaid() != null) event.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());
        if (dto.getTitle() != null) event.setTitle(dto.getTitle());

        if (dto.getStateAction() != null) {
            if (dto.getStateAction() == StateActionUser.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            } else if (dto.getStateAction() == StateActionUser.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
        }
    }

    public void updateEventFromAdminDto(UpdateEventAdminRequest dto, Event event, Category category) {
        if (dto == null) return;

        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (category != null) event.setCategory(category);
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getEventDate() != null) {
            event.setEventDate(LocalDateTime.parse(dto.getEventDate(), FORMATTER));
        }
        if (dto.getLocation() != null) {
            event.setLocation(new Location(dto.getLocation().getLat(), dto.getLocation().getLon()));
        }
        if (dto.getPaid() != null) event.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());
        if (dto.getTitle() != null) event.setTitle(dto.getTitle());

        if (dto.getStateAction() != null) {
            if (dto.getStateAction() == StateActionAdmin.PUBLISH_EVENT) {
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (dto.getStateAction() == StateActionAdmin.REJECT_EVENT) {
                event.setState(EventState.CANCELED);
            }
        }
    }

}

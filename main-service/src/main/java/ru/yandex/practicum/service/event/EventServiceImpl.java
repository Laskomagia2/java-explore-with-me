package ru.yandex.practicum.service.event;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.client.StatsClient;
import ru.yandex.practicum.dal.category.CategoryRepository;
import ru.yandex.practicum.dal.event.EventRepository;
import ru.yandex.practicum.dal.request.RequestRepository;
import ru.yandex.practicum.dal.user.UserRepository;
import ru.yandex.practicum.dto.ViewStatsDto;
import ru.yandex.practicum.dto.event.*;
import ru.yandex.practicum.exception.ConflictException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.mapper.event.EventMapper;
import ru.yandex.practicum.model.category.Category;
import ru.yandex.practicum.model.event.Event;
import ru.yandex.practicum.model.event.EventState;
import ru.yandex.practicum.model.request.RequestStatus;
import ru.yandex.practicum.model.user.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;
    private final EventMapper eventMapper;

    @PersistenceContext
    private final EntityManager entityManager;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<EventShortDto> getEventsByUser(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);

        Map<Long, Long> confirmedRequests = getConfirmedRequests(events);

        return events.stream()
                .map(event -> eventMapper.toEventShortDto(
                        event,
                        confirmedRequests.getOrDefault(event.getId(), 0L),
                        0L
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto dto) {
        LocalDateTime eventDate = LocalDateTime.parse(dto.getEventDate(),
                FORMATTER);
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Field: eventDate. Error: must be in the future. Value: " + eventDate);
        }

        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Category category = categoryRepository.findById(dto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id=" + dto.getCategory() + " was not found"));

        Event event = eventMapper.toEvent(dto, initiator, category);
        return eventMapper.toEventFullDto(eventRepository.save(event), 0L, 0L);
    }

    @Override
    public EventFullDto getEventByUserAndId(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        return eventMapper.toEventFullDto(event, confirmedRequests, 0L);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        if (updateRequest.getEventDate() != null) {
            LocalDateTime newDate = LocalDateTime.parse(updateRequest.getEventDate(), FORMATTER);
            if (newDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException("Event date must be at least 2 hours from now");
            }
        }

        Category category = null;
        if (updateRequest.getCategory() != null) {
            category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
        }

        eventMapper.updateEventFromDto(updateRequest, event, category);
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        return eventMapper.toEventFullDto(eventRepository.save(event), confirmedRequests, 0L);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (updateRequest.getEventDate() != null) {
            LocalDateTime newDate = LocalDateTime.parse(updateRequest.getEventDate(), FORMATTER);
            if (newDate.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ValidationException("Event date must be at least 1 hour from now");
            }
        }

        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction() == StateActionAdmin.PUBLISH_EVENT) {
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Cannot publish the event because it's not in the PENDING state");
                }
            }
            if (updateRequest.getStateAction() == StateActionAdmin.REJECT_EVENT) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Cannot reject the event because it's already PUBLISHED");
                }
            }
        }

        Category category = null;
        if (updateRequest.getCategory() != null) {
            category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
        }

        eventMapper.updateEventFromAdminDto(updateRequest, event, category);

        Long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        return eventMapper.toEventFullDto(eventRepository.save(event), confirmedRequests, 0L);
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                               String rangeStart, String rangeEnd, int from, int size) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = builder.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);
        Predicate criteria = builder.conjunction();

        if (users != null && !users.isEmpty()) {
            criteria = builder.and(criteria, root.get("initiator").get("id").in(users));
        }

        if (states != null && !states.isEmpty()) {
            List<EventState> eventStates = states.stream()
                    .map(EventState::valueOf)
                    .collect(Collectors.toList());
            criteria = builder.and(criteria, root.get("state").in(eventStates));
        }

        if (categories != null && !categories.isEmpty()) {
            criteria = builder.and(criteria, root.get("category").get("id").in(categories));
        }
        if (rangeStart != null) {
            LocalDateTime start = LocalDateTime.parse(rangeStart, FORMATTER);
            criteria = builder.and(criteria, builder.greaterThanOrEqualTo(root.get("eventDate"), start));
        }
        if (rangeEnd != null) {
            LocalDateTime end = LocalDateTime.parse(rangeEnd, FORMATTER);
            criteria = builder.and(criteria, builder.lessThanOrEqualTo(root.get("eventDate"), end));
        }

        query.select(root).where(criteria);

        List<Event> events = entityManager.createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();

        Map<Long, Long> confirmedRequests = getConfirmedRequests(events);
        Map<Long, Long> views = getViews(events);

        return events.stream()
                .map(event -> eventMapper.toEventFullDto(
                        event,
                        confirmedRequests.getOrDefault(event.getId(), 0L),
                        views.getOrDefault(event.getId(), 0L)
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<EventShortDto> getEventsPublic(String text, List<Long> categories, Boolean paid,
                                               String rangeStart, String rangeEnd, Boolean onlyAvailable,
                                               String sort, int from, int size) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = builder.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);

        Predicate criteria = builder.equal(root.get("state"), EventState.PUBLISHED);

        if (text != null && !text.isBlank()) {
            String searchText = "%" + text.toLowerCase() + "%";
            Predicate textInAnnotation = builder.like(builder.lower(root.get("annotation")), searchText);
            Predicate textInDescription = builder.like(builder.lower(root.get("description")), searchText);
            criteria = builder.and(criteria, builder.or(textInAnnotation, textInDescription));
        }

        if (categories != null && !categories.isEmpty()) {
            criteria = builder.and(criteria, root.get("category").get("id").in(categories));
        }
        if (paid != null) {
            criteria = builder.and(criteria, builder.equal(root.get("paid"), paid));
        }

        LocalDateTime start = (rangeStart != null)
                ? LocalDateTime.parse(rangeStart, FORMATTER) : LocalDateTime.now();
        criteria = builder.and(criteria, builder.greaterThanOrEqualTo(root.get("eventDate"), start));

        if (rangeEnd != null) {
            LocalDateTime end = LocalDateTime.parse(rangeEnd, FORMATTER);
            if (end.isBefore(start)) {
                throw new ValidationException("End date cannot be before start date");
            }
            criteria = builder.and(criteria, builder.lessThanOrEqualTo(root.get("eventDate"), end));
        }

        query.select(root).where(criteria);

        if ("EVENT_DATE".equals(sort)) {
            query.orderBy(builder.asc(root.get("eventDate")));
        }

        List<Event> events = entityManager.createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Long> confirmedRequests = getConfirmedRequests(events);

        Map<Long, Long> views = getViews(events);

        List<EventShortDto> eventDtos = events.stream()
                .map(e -> eventMapper.toEventShortDto(
                        e,
                        confirmedRequests.getOrDefault(e.getId(), 0L),
                        views.getOrDefault(e.getId(), 0L)
                ))
                .collect(Collectors.toList());

        if (Boolean.TRUE.equals(onlyAvailable)) {
            eventDtos = eventDtos.stream()
                    .filter(dto -> {
                        Event event = events.stream().filter(e -> e.getId().equals(dto.getId())).findFirst().get();
                        return event.getParticipantLimit() == 0 ||
                                dto.getConfirmedRequests() < event.getParticipantLimit();
                    })
                    .collect(Collectors.toList());
        }

        if ("VIEWS".equals(sort)) {
            eventDtos.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        }

        return eventDtos;
    }

    @Override
    public EventFullDto getEventPublicById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id=" + id + " is not published yet");
        }

        Long confirmedRequests = requestRepository.countByEventIdAndStatus(id, RequestStatus.CONFIRMED);

        Map<Long, Long> viewsMap = getViews(List.of(event));
        Long views = viewsMap.getOrDefault(event.getId(), 0L);

        return eventMapper.toEventFullDto(event, confirmedRequests, views);
    }

    private Map<Long, Long> getViews(List<Event> events) {
        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        String start = LocalDateTime.now().minusYears(10).format(FORMATTER);
        String end = LocalDateTime.now().format(FORMATTER);

        List<ViewStatsDto> stats = statsClient.getStats(start, end, uris, true);

        return stats.stream()
                .collect(Collectors.toMap(
                        dto -> {
                            String uri = dto.getUri();
                            return Long.parseLong(uri.substring(uri.lastIndexOf("/") + 1));
                        },
                        ViewStatsDto::getHits
                ));
    }

    private Map<Long, Long> getConfirmedRequests(List<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());

        return requestRepository.countConfirmedRequestsByEventIds(eventIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

}

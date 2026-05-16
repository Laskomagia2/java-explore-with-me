package ru.yandex.practicum.service.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.client.StatsClient;
import ru.yandex.practicum.dal.category.CategoryRepository;
import ru.yandex.practicum.dal.event.EventRepository;
import ru.yandex.practicum.dal.request.RequestRepository;
import ru.yandex.practicum.dal.user.UserRepository;
import ru.yandex.practicum.dto.LocationDto;
import ru.yandex.practicum.dto.category.CategoryDto;
import ru.yandex.practicum.dto.event.*;
import ru.yandex.practicum.dto.user.UserShortDto;
import ru.yandex.practicum.exception.ConflictException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.model.Location;
import ru.yandex.practicum.model.category.Category;
import ru.yandex.practicum.model.event.Event;
import ru.yandex.practicum.model.event.EventState;
import ru.yandex.practicum.model.request.RequestStatus;
import ru.yandex.practicum.model.user.User;

import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private StatsClient statsClient;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private EventServiceImpl eventService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private User user;
    private Category category;
    private Event event;
    private EventFullDto eventFullDto;
    private EventShortDto eventShortDto;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("user").email("user@email.com").build();
        category = Category.builder().id(1L).name("category").build();

        event = Event.builder()
                .id(1L)
                .title("title")
                .annotation("annotation")
                .description("description")
                .category(category)
                .initiator(user)
                .eventDate(LocalDateTime.now().plusDays(10))
                .location(new Location(55.7f, 37.6f))
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .state(EventState.PENDING)
                .createdOn(LocalDateTime.now())
                .build();

        eventFullDto = EventFullDto.builder()
                .id(1L)
                .annotation("annotation")
                .category(CategoryDto.builder().id(1L).name("category").build())
                .confirmedRequests(0L)
                .createdOn("2026-07-20 20:00:00")
                .description("description")
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
                .annotation("annotation")
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
    void getEventsByUserTest() {
        Pageable pageable = PageRequest.of(0, 10);
        when(eventRepository.findAllByInitiatorId(1L, pageable)).thenReturn(List.of(event));
        when(requestRepository.countConfirmedRequestsByEventIds(any())).thenReturn(List.of());

        List<EventShortDto> result = eventService.getEventsByUser(1L, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("title");
    }

    @Test
    void addEventTest() {
        String futureDate = LocalDateTime.now().plusDays(5).format(FORMATTER);

        NewEventDto newEventDto = NewEventDto.builder()
                .annotation("annotation")
                .category(1L)
                .description("description")
                .eventDate(futureDate)
                .location(LocationDto.builder().lat(55.7f).lon(37.6f).build())
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .title("title")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.save(any())).thenReturn(event);

        EventFullDto result = eventService.addEvent(1L, newEventDto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("title");
        verify(eventRepository, times(1)).save(any());
    }

    @Test
    void addEventWithPastDateTest() {
        String pastDate = LocalDateTime.now().plusMinutes(30).format(FORMATTER);

        NewEventDto newEventDto = NewEventDto.builder()
                .annotation("annotation")
                .category(1L)
                .description("description")
                .eventDate(pastDate)
                .location(LocationDto.builder().lat(55.7f).lon(37.6f).build())
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .title("title")
                .build();

        assertThatThrownBy(() -> eventService.addEvent(1L, newEventDto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void addEventUserNotFoundTest() {
        String futureDate = LocalDateTime.now().plusDays(5).format(FORMATTER);

        NewEventDto newEventDto = NewEventDto.builder()
                .annotation("annotation")
                .category(1L)
                .description("description")
                .eventDate(futureDate)
                .location(LocationDto.builder().lat(55.7f).lon(37.6f).build())
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .title("title")
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.addEvent(999L, newEventDto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getEventByUserAndIdTest() {
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));

        EventFullDto result = eventService.getEventByUserAndId(1L, 1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getEventByUserAndIdNotFoundTest() {
        when(eventRepository.findByIdAndInitiatorId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventByUserAndId(1L, 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateEventByUserTest() {
        UpdateEventUserRequest updateRequest = UpdateEventUserRequest.builder()
                .title("updated")
                .stateAction(StateActionUser.SEND_TO_REVIEW)
                .build();

        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any())).thenReturn(event);

        EventFullDto result = eventService.updateEventByUser(1L, 1L, updateRequest);

        assertThat(result).isNotNull();
    }

    @Test
    void updateEventByUserPublishedTest() {
        event.setState(EventState.PUBLISHED);

        UpdateEventUserRequest updateRequest = UpdateEventUserRequest.builder()
                .title("updated")
                .build();

        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.updateEventByUser(1L, 1L, updateRequest))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateEventByAdminTest() {
        UpdateEventAdminRequest updateRequest = UpdateEventAdminRequest.builder()
                .stateAction(StateActionAdmin.PUBLISH_EVENT)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any())).thenReturn(event);

        EventFullDto result = eventService.updateEventByAdmin(1L, updateRequest);

        assertThat(result).isNotNull();
    }

    @Test
    void updateEventByAdminNotFoundTest() {
        when(eventRepository.findById(anyLong())).thenReturn(Optional.empty());

        UpdateEventAdminRequest updateRequest = UpdateEventAdminRequest.builder()
                .stateAction(StateActionAdmin.PUBLISH_EVENT)
                .build();

        assertThatThrownBy(() -> eventService.updateEventByAdmin(999L, updateRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateEventByAdminPublishNotPendingTest() {
        event.setState(EventState.PUBLISHED);

        UpdateEventAdminRequest updateRequest = UpdateEventAdminRequest.builder()
                .stateAction(StateActionAdmin.PUBLISH_EVENT)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.updateEventByAdmin(1L, updateRequest))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateEventByAdminRejectPublishedTest() {
        event.setState(EventState.PUBLISHED);

        UpdateEventAdminRequest updateRequest = UpdateEventAdminRequest.builder()
                .stateAction(StateActionAdmin.REJECT_EVENT)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.updateEventByAdmin(1L, updateRequest))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void getEventPublicByIdTest() {
        event.setState(EventState.PUBLISHED);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(5L);
        when(statsClient.getStats(any(), any(), any(), anyBoolean())).thenReturn(List.of());

        EventFullDto result = eventService.getEventPublicById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getEventPublicByIdNotFoundTest() {
        when(eventRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventPublicById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getEventPublicByIdNotPublishedTest() {
        event.setState(EventState.PENDING);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.getEventPublicById(1L))
                .isInstanceOf(NotFoundException.class);
    }

}

package ru.yandex.practicum.service.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.dal.event.EventRepository;
import ru.yandex.practicum.dal.request.RequestRepository;
import ru.yandex.practicum.dal.user.UserRepository;
import ru.yandex.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.yandex.practicum.dto.request.ParticipationRequestDto;
import ru.yandex.practicum.exception.ConflictException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.Location;
import ru.yandex.practicum.model.category.Category;
import ru.yandex.practicum.model.event.Event;
import ru.yandex.practicum.model.event.EventState;
import ru.yandex.practicum.model.request.Request;
import ru.yandex.practicum.model.request.RequestStatus;
import ru.yandex.practicum.model.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestServiceImplTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RequestServiceImpl requestService;

    private User user;
    private User initiator;
    private Event event;
    private Request request;
    private ParticipationRequestDto requestDto;

    @BeforeEach
    void setUp() {
        initiator = User.builder().id(1L).name("initiator").email("init@email.com").build();
        user = User.builder().id(2L).name("user").email("user@email.com").build();

        event = Event.builder()
                .id(1L)
                .title("title")
                .annotation("annotation")
                .description("description")
                .category(Category.builder().id(1L).name("cat").build())
                .initiator(initiator)
                .eventDate(LocalDateTime.now().plusDays(10))
                .location(new Location(55.7f, 37.6f))
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .state(EventState.PUBLISHED)
                .createdOn(LocalDateTime.now())
                .build();

        request = Request.builder()
                .id(1L)
                .event(event)
                .requester(user)
                .status(RequestStatus.PENDING)
                .created(LocalDateTime.now())
                .build();

        requestDto = ParticipationRequestDto.builder()
                .id(1L)
                .created("2026-07-20 20:00:00")
                .event(1L)
                .requester(2L)
                .status("PENDING")
                .build();
    }

    @Test
    void getRequestsTest() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(requestRepository.findAllByRequesterId(2L)).thenReturn(List.of(request));

        List<ParticipationRequestDto> result = requestService.getRequests(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getRequestsUserNotFoundTest() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        assertThatThrownBy(() -> requestService.getRequests(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createRequestTest() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.existsByRequesterIdAndEventId(2L, 1L)).thenReturn(false);
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(0L);
        when(requestRepository.save(any())).thenReturn(request);

        ParticipationRequestDto result = requestService.createRequest(2L, 1L);

        assertThat(result.getId()).isEqualTo(1L);
        verify(requestRepository, times(1)).save(any());
    }

    @Test
    void createRequestDuplicateTest() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.existsByRequesterIdAndEventId(2L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> requestService.createRequest(2L, 1L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createRequestOwnEventTest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(initiator));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.existsByRequesterIdAndEventId(1L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> requestService.createRequest(1L, 1L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createRequestUnpublishedEventTest() {
        Event pendingEvent = Event.builder()
                .id(2L)
                .title("title")
                .annotation("annotation")
                .description("description")
                .category(Category.builder().id(1L).name("cat").build())
                .initiator(initiator)
                .eventDate(LocalDateTime.now().plusDays(10))
                .location(new Location(55.7f, 37.6f))
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .state(EventState.PENDING)
                .createdOn(LocalDateTime.now())
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(2L)).thenReturn(Optional.of(pendingEvent));
        when(requestRepository.existsByRequesterIdAndEventId(2L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> requestService.createRequest(2L, 2L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createRequestLimitReachedTest() {
        Event limitedEvent = Event.builder()
                .id(3L)
                .title("title")
                .annotation("annotation")
                .description("description")
                .category(Category.builder().id(1L).name("cat").build())
                .initiator(initiator)
                .eventDate(LocalDateTime.now().plusDays(10))
                .location(new Location(55.7f, 37.6f))
                .paid(false)
                .participantLimit(1)
                .requestModeration(true)
                .state(EventState.PUBLISHED)
                .createdOn(LocalDateTime.now())
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(3L)).thenReturn(Optional.of(limitedEvent));
        when(requestRepository.existsByRequesterIdAndEventId(2L, 3L)).thenReturn(false);
        when(requestRepository.countByEventIdAndStatus(3L, RequestStatus.CONFIRMED)).thenReturn(1L);

        assertThatThrownBy(() -> requestService.createRequest(2L, 3L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void cancelRequestTest() {
        ParticipationRequestDto canceledDto = ParticipationRequestDto.builder()
                .id(1L)
                .created("2026-07-20 20:00:00")
                .event(1L)
                .requester(2L)
                .status("CANCELED")
                .build();

        when(requestRepository.findByIdAndRequesterId(1L, 2L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any())).thenReturn(request);

        ParticipationRequestDto result = requestService.cancelRequest(2L, 1L);

        assertThat(result.getStatus()).isEqualTo("CANCELED");
    }

    @Test
    void cancelRequestNotFoundTest() {
        when(requestRepository.findByIdAndRequesterId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.cancelRequest(2L, 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getEventRequestsTest() {
        when(eventRepository.existsByIdAndInitiatorId(1L, 1L)).thenReturn(true);
        when(requestRepository.findAllByEventId(1L)).thenReturn(List.of(request));

        List<ParticipationRequestDto> result = requestService.getEventRequests(1L, 1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getEventRequestsNotOwnerTest() {
        when(eventRepository.existsByIdAndInitiatorId(1L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> requestService.getEventRequests(2L, 1L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateRequestStatusConfirmTest() {
        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(1L));
        updateRequest.setStatus("CONFIRMED");

        ParticipationRequestDto confirmedDto = ParticipationRequestDto.builder()
                .id(1L).created("2026-07-20 20:00:00").event(1L).requester(2L).status("CONFIRMED").build();

        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));
        when(requestRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(request));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(0L);

        EventRequestStatusUpdateResult result = requestService.updateRequestStatus(1L, 1L, updateRequest);

        assertThat(result.getConfirmedRequests()).hasSize(1);
        assertThat(result.getRejectedRequests()).isEmpty();
    }

    @Test
    void updateRequestStatusRejectTest() {
        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(1L));
        updateRequest.setStatus("REJECTED");

        ParticipationRequestDto rejectedDto = ParticipationRequestDto.builder()
                .id(1L).created("2026-07-20 20:00:00").event(1L).requester(2L).status("REJECTED").build();

        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));
        when(requestRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(request));

        EventRequestStatusUpdateResult result = requestService.updateRequestStatus(1L, 1L, updateRequest);

        assertThat(result.getConfirmedRequests()).isEmpty();
        assertThat(result.getRejectedRequests()).hasSize(1);
    }

    @Test
    void updateRequestStatusNotPendingTest() {
        Request confirmedRequest = Request.builder()
                .id(1L).event(event).requester(user)
                .status(RequestStatus.CONFIRMED).created(LocalDateTime.now()).build();

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(1L));
        updateRequest.setStatus("REJECTED");

        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));
        when(requestRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(confirmedRequest));

        assertThatThrownBy(() -> requestService.updateRequestStatus(1L, 1L, updateRequest))
                .isInstanceOf(ConflictException.class);
    }

}

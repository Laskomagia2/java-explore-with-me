package ru.yandex.practicum.service.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dal.event.EventRepository;
import ru.yandex.practicum.dal.request.RequestRepository;
import ru.yandex.practicum.dal.user.UserRepository;
import ru.yandex.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.yandex.practicum.dto.request.ParticipationRequestDto;
import ru.yandex.practicum.exception.ConflictException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.mapper.request.RequestMapper;
import ru.yandex.practicum.model.event.Event;
import ru.yandex.practicum.model.event.EventState;
import ru.yandex.practicum.model.request.Request;
import ru.yandex.practicum.model.request.RequestStatus;
import ru.yandex.practicum.model.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getRequests(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Заявка уже подана");
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Нельзя подать заявку на свое событие");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Событие еще не опубликовано");
        }

        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() != 0 && confirmedCount >= event.getParticipantLimit()) {
            throw new ConflictException("Лимит участников исчерпан");
        }

        RequestStatus status = (!event.getRequestModeration() || event.getParticipantLimit() == 0)
                ? RequestStatus.CONFIRMED : RequestStatus.PENDING;

        Request request = Request.builder()
                .event(event)
                .requester(requester)
                .created(LocalDateTime.now())
                .status(status)
                .build();

        return requestMapper.toDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Request request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Request not found"));
        request.setStatus(RequestStatus.CANCELED);
        return requestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        if (!eventRepository.existsByIdAndInitiatorId(eventId, userId)) {
            throw new ConflictException("Это не ваше событие");
        }
        return requestRepository.findAllByEventId(eventId).stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        List<Request> requests = requestRepository.findAllByIdIn(updateRequest.getRequestIds());

        for (Request r : requests) {
            if (r.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Можно изменять только заявки в статусе PENDING");
            }
        }

        EventRequestStatusUpdateResult result = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(new ArrayList<>())
                .rejectedRequests(new ArrayList<>())
                .build();

        if (updateRequest.getStatus().equals("REJECTED")) {
            for (Request r : requests) {
                r.setStatus(RequestStatus.REJECTED);
                result.getRejectedRequests().add(requestMapper.toDto(r));
            }
        } else {
            long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            int limit = event.getParticipantLimit();

            if (limit != 0 && confirmedCount >= limit) {
                throw new ConflictException("Лимит участников уже заполнен");
            }

            for (Request r : requests) {
                if (limit == 0 || confirmedCount < limit) {
                    r.setStatus(RequestStatus.CONFIRMED);
                    result.getConfirmedRequests().add(requestMapper.toDto(r));
                    confirmedCount++;
                } else {
                    r.setStatus(RequestStatus.REJECTED);
                    result.getRejectedRequests().add(requestMapper.toDto(r));
                }
            }
        }

        requestRepository.saveAll(requests);
        return result;
    }
}

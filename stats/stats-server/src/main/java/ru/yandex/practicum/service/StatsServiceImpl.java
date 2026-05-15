package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.EndpointHitDto;
import ru.yandex.practicum.dal.StatsRepository;
import ru.yandex.practicum.dto.ViewStatsDto;
import ru.yandex.practicum.exception.InvalidRequestParams;
import ru.yandex.practicum.mapper.HitMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    @Transactional
    public void saveHit(EndpointHitDto hitDto) {
        statsRepository.save(HitMapper.toHit(hitDto));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {

        if (start != null && end != null && start.isAfter(end)) {
            throw new InvalidRequestParams("End date cannot be before start date");
        }

        if (unique) {
            return statsRepository.getUniqueStats(start, end, uris);
        } else {
            return statsRepository.getStats(start, end, uris);
        }
    }

}
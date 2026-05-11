package service;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.StatsServerApplication;
import ru.yandex.practicum.dto.EndpointHitDto;
import ru.yandex.practicum.dto.ViewStatsDto;
import ru.yandex.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = StatsServerApplication.class)
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class StatsServiceIntegrationTest {

    private final StatsService service;

    @Test
    void shouldSaveHitAndGetStats() {
        EndpointHitDto hit = EndpointHitDto.builder()
                .app("main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(LocalDateTime.now().minusMinutes(10))
                .build();

        service.saveHit(hit);

        List<ViewStatsDto> stats = service.getStats(
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1),
                List.of("/events/1"),
                false
        );

        assertThat(stats).hasSize(1);
        assertThat(stats.get(0).getApp()).isEqualTo("main-service");
        assertThat(stats.get(0).getHits()).isEqualTo(1L);
    }

    @Test
    void shouldReturnUniqueStats() {
        EndpointHitDto hit1 = EndpointHitDto.builder()
                .app("main-service").uri("/events/1").ip("192.168.1.1").timestamp(LocalDateTime.now()).build();
        EndpointHitDto hit2 = EndpointHitDto.builder()
                .app("main-service").uri("/events/1").ip("192.168.1.1").timestamp(LocalDateTime.now()).build();

        service.saveHit(hit1);
        service.saveHit(hit2);

        List<ViewStatsDto> uniqueStats = service.getStats(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                null,
                true
        );

        assertThat(uniqueStats.get(0).getHits()).isEqualTo(1L);
    }
}

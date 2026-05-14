package ru.yandex.practicum.service.compilation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.dal.compilation.CompilationRepository;
import ru.yandex.practicum.dal.event.EventRepository;
import ru.yandex.practicum.dto.compilation.CompilationDto;
import ru.yandex.practicum.dto.compilation.NewCompilationDto;
import ru.yandex.practicum.dto.compilation.UpdateCompilationRequest;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.mapper.compilation.CompilationMapper;
import ru.yandex.practicum.model.compilation.Compilation;
import ru.yandex.practicum.service.event.EventServiceImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompilationServiceImplTest {

    @Mock
    private CompilationRepository compilationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CompilationMapper compilationMapper;

    @Mock
    private EventServiceImpl eventService;

    @InjectMocks
    private CompilationServiceImpl compilationService;

    private Compilation compilation;
    private CompilationDto compilationDto;

    @BeforeEach
    void setUp() {
        compilation = Compilation.builder()
                .id(1L)
                .title("title")
                .pinned(true)
                .events(new HashSet<>())
                .build();

        compilationDto = CompilationDto.builder()
                .id(1L)
                .title("title")
                .pinned(true)
                .events(List.of())
                .build();
    }

    @Test
    void addCompilationTest() {
        NewCompilationDto newDto = new NewCompilationDto();
        newDto.setTitle("title");
        newDto.setPinned(true);

        when(compilationMapper.toEntity(any(), any())).thenReturn(compilation);
        when(compilationRepository.save(any())).thenReturn(compilation);
        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));
        when(compilationMapper.toDto(compilation)).thenReturn(compilationDto);

        CompilationDto result = compilationService.addCompilation(newDto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("title");
        assertThat(result.getPinned()).isTrue();
        verify(compilationRepository, times(1)).save(any());
    }

    @Test
    void addCompilationWithEventsTest() {
        NewCompilationDto newDto = new NewCompilationDto();
        newDto.setTitle("title");
        newDto.setPinned(true);
        newDto.setEvents(List.of(1L));

        when(eventRepository.findAllById(any())).thenReturn(List.of());
        when(compilationMapper.toEntity(any(), any())).thenReturn(compilation);
        when(compilationRepository.save(any())).thenReturn(compilation);
        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));
        when(compilationMapper.toDto(compilation)).thenReturn(compilationDto);

        CompilationDto result = compilationService.addCompilation(newDto);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void deleteCompilationTest() {
        when(compilationRepository.existsById(1L)).thenReturn(true);

        compilationService.deleteCompilation(1L);

        verify(compilationRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCompilationNotFoundTest() {
        when(compilationRepository.existsById(anyLong())).thenReturn(false);

        assertThatThrownBy(() -> compilationService.deleteCompilation(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateCompilationTest() {
        UpdateCompilationRequest request = new UpdateCompilationRequest();
        request.setTitle("newTitle");
        request.setPinned(false);

        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));
        when(compilationRepository.save(any())).thenReturn(compilation);
        when(compilationMapper.toDto(compilation)).thenReturn(
                CompilationDto.builder().id(1L).title("newTitle").pinned(false).events(List.of()).build()
        );

        CompilationDto result = compilationService.updateCompilation(1L, request);

        assertThat(result.getTitle()).isEqualTo("newTitle");
        assertThat(result.getPinned()).isFalse();
    }

    @Test
    void updateCompilationNotFoundTest() {
        when(compilationRepository.findById(anyLong())).thenReturn(Optional.empty());

        UpdateCompilationRequest request = new UpdateCompilationRequest();
        request.setTitle("newTitle");

        assertThatThrownBy(() -> compilationService.updateCompilation(999L, request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getCompilationsWithPinnedTest() {
        Pageable pageable = PageRequest.of(0, 10);
        when(compilationRepository.findAllByPinned(true, pageable)).thenReturn(List.of(compilation));
        when(compilationMapper.toDto(compilation)).thenReturn(compilationDto);

        List<CompilationDto> result = compilationService.getCompilations(true, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPinned()).isTrue();
    }

    @Test
    void getCompilationsWithoutPinnedTest() {
        Pageable pageable = PageRequest.of(0, 10);
        when(compilationRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(compilation)));
        when(compilationMapper.toDto(compilation)).thenReturn(compilationDto);

        List<CompilationDto> result = compilationService.getCompilations(null, 0, 10);

        assertThat(result).hasSize(1);
    }

    @Test
    void getCompilationTest() {
        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));
        when(compilationMapper.toDto(compilation)).thenReturn(compilationDto);

        CompilationDto result = compilationService.getCompilation(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("title");
    }

    @Test
    void getCompilationNotFoundTest() {
        when(compilationRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> compilationService.getCompilation(999L))
                .isInstanceOf(NotFoundException.class);
    }

}

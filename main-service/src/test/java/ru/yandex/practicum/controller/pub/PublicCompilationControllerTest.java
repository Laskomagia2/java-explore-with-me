package ru.yandex.practicum.controller.pub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.dto.compilation.CompilationDto;
import ru.yandex.practicum.service.compilation.CompilationService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicCompilationController.class)
public class PublicCompilationControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CompilationService compilationService;

    private CompilationDto compilationDto;

    @BeforeEach
    void setUp() {
        compilationDto = CompilationDto.builder()
                .id(1L)
                .title("title")
                .pinned(true)
                .events(List.of())
                .build();
    }

    @Test
    void getCompilationsTest() throws Exception {
        when(compilationService.getCompilations(any(), anyInt(), anyInt()))
                .thenReturn(List.of(compilationDto));

        mvc.perform(get("/compilations")
                        .param("from", "0")
                        .param("size", "10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(compilationDto.getId().intValue())))
                .andExpect(jsonPath("$[0].title", is(compilationDto.getTitle())));
    }

    @Test
    void getCompilationsWithPinnedFilterTest() throws Exception {
        when(compilationService.getCompilations(eq(true), anyInt(), anyInt()))
                .thenReturn(List.of(compilationDto));

        mvc.perform(get("/compilations")
                        .param("pinned", "true")
                        .param("from", "0")
                        .param("size", "10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].pinned", is(true)));
    }

    @Test
    void getCompilationTest() throws Exception {
        when(compilationService.getCompilation(anyLong())).thenReturn(compilationDto);

        mvc.perform(get("/compilations/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(compilationDto.getId().intValue())))
                .andExpect(jsonPath("$.title", is(compilationDto.getTitle())))
                .andExpect(jsonPath("$.pinned", is(compilationDto.getPinned())));
    }

}

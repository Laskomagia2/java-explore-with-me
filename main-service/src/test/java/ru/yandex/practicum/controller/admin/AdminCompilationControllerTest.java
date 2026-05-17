package ru.yandex.practicum.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.dto.compilation.CompilationDto;
import ru.yandex.practicum.dto.compilation.NewCompilationDto;
import ru.yandex.practicum.dto.compilation.UpdateCompilationRequest;
import ru.yandex.practicum.service.compilation.CompilationService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminCompilationController.class)
public class AdminCompilationControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

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
    void addCompilationTest() throws Exception {
        NewCompilationDto request = new NewCompilationDto();
        request.setTitle("title");
        request.setPinned(true);
        request.setEvents(List.of(1L));

        when(compilationService.addCompilation(any())).thenReturn(compilationDto);

        mvc.perform(post("/admin/compilations")
                        .content(objectMapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(compilationDto.getId().intValue())))
                .andExpect(jsonPath("$.title", is(compilationDto.getTitle())))
                .andExpect(jsonPath("$.pinned", is(compilationDto.getPinned())));
    }

    @Test
    void deleteCompilationTest() throws Exception {
        doNothing().when(compilationService).deleteCompilation(anyLong());

        mvc.perform(delete("/admin/compilations/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateCompilationTest() throws Exception {
        UpdateCompilationRequest request = new UpdateCompilationRequest();
        request.setTitle("newTitle");
        request.setPinned(false);

        CompilationDto updatedDto = CompilationDto.builder()
                .id(1L)
                .title("newTitle")
                .pinned(false)
                .events(List.of())
                .build();

        when(compilationService.updateCompilation(anyLong(), any())).thenReturn(updatedDto);

        mvc.perform(patch("/admin/compilations/1")
                        .content(objectMapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedDto.getId().intValue())))
                .andExpect(jsonPath("$.title", is(updatedDto.getTitle())))
                .andExpect(jsonPath("$.pinned", is(updatedDto.getPinned())));
    }

}

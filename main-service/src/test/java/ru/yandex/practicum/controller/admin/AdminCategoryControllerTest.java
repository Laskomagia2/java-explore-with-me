package ru.yandex.practicum.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.dal.category.CategoryRepository;
import ru.yandex.practicum.dto.category.CategoryDto;
import ru.yandex.practicum.dto.category.NewCategoryDto;
import ru.yandex.practicum.service.category.CategoryService;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@WebMvcTest(controllers = AdminCategoryController.class)
public class AdminCategoryControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private CategoryRepository categoryRepository;

    private CategoryDto dto;

    @BeforeEach
    void dtoSetUp() {
        dto = CategoryDto.builder()
                .id(1L)
                .name("name")
                .build();
    }

    @Test
    void addCategoryTest() throws Exception {
        NewCategoryDto request = NewCategoryDto.builder()
                .name("name")
                .build();

        when(categoryService.addCategory(any())).thenReturn(dto);

        mvc.perform(post("/admin/categories")
                            .content(objectMapper.writeValueAsString((request)))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(dto.getId().intValue())))
                    .andExpect(jsonPath("$.name", is(dto.getName())));
    }

    @Test
    void deleteCategoryTest() throws Exception {
        mvc.perform(delete("/admin/categories/1")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());
    }

    @Test
    void updateCategoryTest() throws Exception {
        CategoryDto newDto = CategoryDto.builder()
                .id(1L)
                .name("newName")
                .build();

        when(categoryService.updateCategory(anyLong(), any())).thenReturn(dto);

        mvc.perform(patch("/admin/categories/1")
                            .content(objectMapper.writeValueAsString(newDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(dto.getId().intValue())))
                    .andExpect(jsonPath("$.name", is(dto.getName())));
    }

}

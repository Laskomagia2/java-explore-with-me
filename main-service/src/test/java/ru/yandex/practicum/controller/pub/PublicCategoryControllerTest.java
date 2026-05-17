package ru.yandex.practicum.controller.pub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.dto.category.CategoryDto;
import ru.yandex.practicum.service.category.CategoryService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicCategoryController.class)
public class PublicCategoryControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CategoryService categoryService;

    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        categoryDto = CategoryDto.builder()
                .id(1L)
                .name("category")
                .build();
    }

    @Test
    void getCategoriesTest() throws Exception {
        when(categoryService.getCategories(anyInt(), anyInt())).thenReturn(List.of(categoryDto));

        mvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(categoryDto.getId().intValue())))
                .andExpect(jsonPath("$[0].name", is(categoryDto.getName())));
    }

    @Test
    void getCategoryTest() throws Exception {
        when(categoryService.getCategory(anyLong())).thenReturn(categoryDto);

        mvc.perform(get("/categories/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(categoryDto.getId().intValue())))
                .andExpect(jsonPath("$.name", is(categoryDto.getName())));
    }

}

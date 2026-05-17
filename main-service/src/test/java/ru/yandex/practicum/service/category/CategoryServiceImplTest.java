package ru.yandex.practicum.service.category;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.dal.category.CategoryRepository;
import ru.yandex.practicum.dal.event.EventRepository;
import ru.yandex.practicum.dto.category.CategoryDto;
import ru.yandex.practicum.dto.category.NewCategoryDto;
import ru.yandex.practicum.exception.ConflictException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.category.Category;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("category")
                .build();

        categoryDto = CategoryDto.builder()
                .id(1L)
                .name("category")
                .build();
    }

    @Test
    void addCategoryTest() {
        NewCategoryDto newCategoryDto = NewCategoryDto.builder().name("category").build();

        when(categoryRepository.save(any())).thenReturn(category);

        CategoryDto result = categoryService.addCategory(newCategoryDto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("category");
        verify(categoryRepository, times(1)).save(any());
    }

    @Test
    void deleteCategoryTest() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.existsByCategoryId(1L)).thenReturn(false);

        categoryService.deleteCategory(1L);

        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCategoryNotFoundTest() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteCategoryWithEventsTest() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.existsByCategoryId(1L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateCategoryTest() {
        CategoryDto updateDto = CategoryDto.builder().id(1L).name("newName").build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any())).thenReturn(category);

        CategoryDto result = categoryService.updateCategory(1L, updateDto);

        assertThat(result.getName()).isEqualTo("newName");
        verify(categoryRepository, times(1)).save(any());
    }

    @Test
    void updateCategoryNotFoundTest() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(999L, categoryDto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getCategoriesTest() {
        Pageable pageable = PageRequest.of(0, 10);
        when(categoryRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(category)));

        List<CategoryDto> result = categoryService.getCategories(0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("category");
    }

    @Test
    void getCategoryTest() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryDto result = categoryService.getCategory(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("category");
    }

    @Test
    void getCategoryNotFoundTest() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategory(999L))
                .isInstanceOf(NotFoundException.class);
    }

}

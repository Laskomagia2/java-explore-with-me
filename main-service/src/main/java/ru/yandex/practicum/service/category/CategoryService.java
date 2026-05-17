package ru.yandex.practicum.service.category;

import ru.yandex.practicum.dto.category.CategoryDto;
import ru.yandex.practicum.dto.category.NewCategoryDto;

import java.util.List;


public interface CategoryService {

    CategoryDto addCategory(NewCategoryDto newCategoryDto);

    void deleteCategory(Long catId);

    CategoryDto updateCategory(Long catId, CategoryDto categoryDto);

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategory(Long catId);

}

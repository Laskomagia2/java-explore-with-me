package ru.yandex.practicum.dal.category;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.category.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}

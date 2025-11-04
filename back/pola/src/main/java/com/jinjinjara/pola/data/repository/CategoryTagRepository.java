package com.jinjinjara.pola.data.repository;

import com.jinjinjara.pola.data.entity.CategoryTag;
import com.jinjinjara.pola.data.entity.Category;
import com.jinjinjara.pola.data.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryTagRepository extends JpaRepository<CategoryTag, Long> {
    List<CategoryTag> findByCategory(Category category);
    Optional<CategoryTag> findByCategoryAndTag(Category category, Tag tag);
    void deleteByCategoryAndTag(Category category, Tag tag);
}

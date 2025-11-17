package com.jinjinjara.pola.data.repository;

import com.jinjinjara.pola.data.entity.CategoryTag;
import com.jinjinjara.pola.data.entity.Category;
import com.jinjinjara.pola.data.entity.Tag;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryTagRepository extends JpaRepository<CategoryTag, Long> {
    List<CategoryTag> findByCategory(Category category);

    Optional<CategoryTag> findByCategoryAndTag(Category category, Tag tag);

    void deleteByCategoryAndTag(Category category, Tag tag);

    boolean existsByCategoryAndTag(Category category, Tag tag);

    void deleteByCategoryId(Long categoryId);

    @Query("""
            SELECT t
            FROM CategoryTag ct
            JOIN ct.tag t
            WHERE ct.category.id = :categoryId
            """)
    List<Tag> findTagsByCategoryId(@Param("categoryId") Long categoryId);

    boolean existsByTag(Tag tag);


    @Query("""
                SELECT DISTINCT ct 
                FROM CategoryTag ct
                JOIN FETCH ct.category c
                JOIN FETCH ct.tag t
                WHERE c.user.id = :userId
            """)
    List<CategoryTag> findAllByUserId(@Param("userId") Long userId);


}

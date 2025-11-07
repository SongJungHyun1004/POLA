package com.jinjinjara.pola.data.repository;

import com.jinjinjara.pola.data.entity.Tag;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    // 중복 방지를 위한 선택적 메서드 (필요시 사용)
    Optional<Tag> findByTagName(String tagName);

    boolean existsByTagName(String tagName);

    @Query("""
        SELECT t FROM Tag t
        JOIN FileTag ft ON t.id = ft.tag.id
        WHERE ft.file.id = :fileId
    """)
    List<Tag> findAllByFileId(@Param("fileId") Long fileId);

}

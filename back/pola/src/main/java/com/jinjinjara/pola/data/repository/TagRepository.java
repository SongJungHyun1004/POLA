package com.jinjinjara.pola.data.repository;

import com.jinjinjara.pola.data.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    // 중복 방지를 위한 선택적 메서드 (필요시 사용)
    Optional<Tag> findByTagName(String tagName);

    boolean existsByTagName(String tagName);
}

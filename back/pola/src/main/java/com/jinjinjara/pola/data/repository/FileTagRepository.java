package com.jinjinjara.pola.data.repository;

import com.jinjinjara.pola.data.entity.FileTag;
import com.jinjinjara.pola.data.entity.File;
import com.jinjinjara.pola.data.entity.Tag;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FileTagRepository extends JpaRepository<FileTag, Long> {

    // 특정 파일에 연결된 모든 태그 조회
    List<FileTag> findByFile(File file);

    // 파일과 태그 조합으로 조회 (중복 여부 확인 등)
    Optional<FileTag> findByFileAndTag(File file, Tag tag);
    @Query("""
SELECT ft
FROM FileTag ft
JOIN FETCH ft.tag
WHERE ft.file.id IN :fileIds
""")
    List<FileTag> findAllByFileIds(@Param("fileIds") List<Long> fileIds);
    void deleteByFileAndTag(File file, Tag tag);

    boolean existsByFileAndTag(File file, Tag tag);

}

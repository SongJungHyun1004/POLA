package com.jinjinjara.pola.data.repository;

import com.jinjinjara.pola.data.entity.FileTag;
import com.jinjinjara.pola.data.entity.File;
import com.jinjinjara.pola.data.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileTagRepository extends JpaRepository<FileTag, Long> {

    // 특정 파일에 연결된 모든 태그 조회
    List<FileTag> findByFile(File file);

    // 파일과 태그 조합으로 조회 (중복 여부 확인 등)
    Optional<FileTag> findByFileAndTag(File file, Tag tag);

    // 파일에서 특정 태그 연결 해제
    void deleteByFileAndTag(File file, Tag tag);
}

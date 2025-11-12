package com.jinjinjara.pola.data.repository;

import com.jinjinjara.pola.data.entity.FileTag;
import com.jinjinjara.pola.data.entity.File;
import com.jinjinjara.pola.data.entity.Tag;
import com.jinjinjara.pola.data.dto.response.TagWithLatestFileDto;
import com.jinjinjara.pola.rag.dto.common.TagRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FileTagRepository extends JpaRepository<FileTag, Long> {

    List<FileTag> findByFile(File file);

    Optional<FileTag> findByFileAndTag(File file, Tag tag);

    @Query("SELECT ft FROM FileTag ft JOIN FETCH ft.tag WHERE ft.file.id IN :fileIds")
    List<FileTag> findAllByFileIds(@Param("fileIds") List<Long> fileIds);

    void deleteByFileAndTag(File file, Tag tag);

    boolean existsByFileAndTag(File file, Tag tag);
    void deleteByFile(File file);

    @Query("SELECT new com.jinjinjara.pola.data.dto.response.TagWithLatestFileDto(" +
            "t.id, t.tagName, COUNT(ft.file.id), MAX(f.createdAt)) " +
            "FROM FileTag ft " +
            "JOIN ft.tag t " +
            "JOIN ft.file f " +
            "WHERE f.categoryId = :categoryId " +
            "GROUP BY t.id, t.tagName " +
            "ORDER BY COUNT(ft.file.id) DESC, MAX(f.createdAt) DESC")
    List<TagWithLatestFileDto> findTagStatsByCategory(@Param("categoryId") Long categoryId);

    Optional<FileTag> findFirstByFile_CategoryIdAndTag_IdOrderByFile_CreatedAtDescFile_IdDesc(Long categoryId, Long tagId);


    @Query("""
    SELECT ft.file
    FROM FileTag ft
    JOIN ft.file f
    WHERE f.categoryId = :categoryId
      AND ft.tag.id = :tagId
    ORDER BY f.createdAt DESC, f.id DESC
    """)
    Optional<File> findLatestFileByCategoryAndTag(@Param("categoryId") Long categoryId,
                                                  @Param("tagId") Long tagId);
}

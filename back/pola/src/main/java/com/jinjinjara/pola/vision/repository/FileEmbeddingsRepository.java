package com.jinjinjara.pola.vision.repository;


import com.jinjinjara.pola.rag.dto.common.SearchRow;
import com.jinjinjara.pola.vision.entity.FileEmbeddings;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileEmbeddingsRepository extends JpaRepository<FileEmbeddings, Long> {

    // ------- 조회 -------
    List<FileEmbeddings> findByUserId(Long userId);

    List<FileEmbeddings> findByFile_Id(Long fileId);

    Optional<FileEmbeddings> findByUserIdAndFile_Id(Long userId, Long fileId);

    Optional<FileEmbeddings> findTopByFile_IdOrderByCreatedAtDesc(Long fileId);

    // ------- 부분 업데이트 -------
    // embedding + ocrText + context 한 번에 갱신
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
           update FileEmbeddings fe
              set fe.embedding = :embedding,
                  fe.ocrText   = :ocrText,
                  fe.context   = :context
            where fe.id        = :id
           """)
    int updateAll(@Param("id") Long id,
                  @Param("embedding") float[] embedding,
                  @Param("ocrText") String ocrText,
                  @Param("context") String context);

    // embedding만 갱신
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update FileEmbeddings fe set fe.embedding = :embedding where fe.id = :id")
    int updateEmbedding(@Param("id") Long id, @Param("embedding") float[] embedding);

    // 텍스트만 갱신
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
           update FileEmbeddings fe
              set fe.ocrText = :ocrText,
                  fe.context = :context
            where fe.id      = :id
           """)

    int updateTexts(@Param("id") Long id,
                    @Param("ocrText") String ocrText,
                    @Param("context") String context);

    @Query(value = """
      SELECT
        f.id AS id,
        f.src AS src,
        f.type AS type,
        f.favorite AS favorite,
        fe.ocr_text AS ocr_text,
        fe.context AS context,
        1 - (fe.embedding <-> CAST(:vec AS vector(768))) AS relevance_score,
        f.created_at AS created_at
      FROM file_embeddings fe
      JOIN files f ON f.id = fe.file_id
      WHERE fe.user_id = :userId
      ORDER BY fe.embedding <-> CAST(:vec AS vector(768))
      LIMIT :limit
      """, nativeQuery = true)
    List<SearchRow> findSimilarFilesWithScore(@Param("userId") Long userId,
                                              @Param("vec") String vectorLiteral,
                                              @Param("limit") int limit);

    @Query(value = """
      SELECT
        f.id AS id,
        f.src AS src,
        f.type AS type,
        f.favorite AS favorite,
        fe.ocr_text AS ocr_text,
        fe.context AS context,
        1 - (fe.embedding <-> CAST(:vec AS vector(768))) AS relevance_score,
        f.created_at AS created_at
      FROM file_embeddings fe
      JOIN files f ON f.id = fe.file_id
      WHERE fe.user_id = :userId
        AND fe.created_at BETWEEN :startTs AND :endTs
      ORDER BY fe.embedding <-> CAST(:vec AS vector(768))
      LIMIT :limit
      """, nativeQuery = true)
    List<SearchRow> findSimilarFilesWithScoreAndDate(
            @Param("userId") Long userId,
            @Param("vec") String vectorLiteral,
            @Param("limit") int limit,
            @Param("startTs") LocalDateTime startTs,
            @Param("endTs") LocalDateTime endTs
    );
}


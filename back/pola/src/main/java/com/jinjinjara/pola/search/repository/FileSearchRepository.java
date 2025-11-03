//package com.jinjinjara.pola.search.repository;
//
//import com.jinjinjara.pola.search.document.FileDocument;
//import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface FileSearchRepository extends ElasticsearchRepository<FileDocument, Long> {
//
//    // 키워드 검색
//    List<FileDocument> findByTagsContainingOrContextContainingOrOcrTextContaining(
//            String tag, String context, String ocrText
//    );
//
//    // 카테고리별 조회
//    List<FileDocument> findByCategoryName(String categoryName);
//}

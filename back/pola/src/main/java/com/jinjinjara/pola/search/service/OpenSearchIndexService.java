package com.jinjinjara.pola.search.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * OpenSearch 인덱스 초기화 서비스
 * 애플리케이션 시작 시 자동으로 files 인덱스를 생성합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenSearchIndexService {

    private final OpenSearchClient client;
    private final ObjectMapper objectMapper;
    private static final String INDEX_NAME = "files";

    /**
     * 애플리케이션 시작 시 인덱스 생성 (존재하지 않는 경우)
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeIndex() {
        try {
            // 인덱스 존재 여부 확인
            boolean exists = client.indices()
                    .exists(ExistsRequest.of(e -> e.index(INDEX_NAME)))
                    .value();

            if (!exists) {
                log.info("========================================");
                log.info("OpenSearch 인덱스 '{}' 생성 중...", INDEX_NAME);
                log.info("========================================");

                // opensearch-mapping.json 파일 로드
                try (InputStream mappingStream = new ClassPathResource("opensearch-mapping.json")
                        .getInputStream()) {

                    // JSON을 문자열로 읽기
                    String mappingJson = new String(mappingStream.readAllBytes(), StandardCharsets.UTF_8);

                    // Raw JSON을 직접 OpenSearch에 전송 (가장 확실한 방법)
                    CreateIndexRequest request = CreateIndexRequest.of(b -> b
                            .index(INDEX_NAME)
                            .withJson(new java.io.StringReader(mappingJson))
                    );

                    client.indices().create(request);
                }

                log.info("✅ OpenSearch 인덱스 '{}' 생성 완료", INDEX_NAME);
            } else {
                log.info("✅ OpenSearch 인덱스 '{}' 이미 존재함", INDEX_NAME);
            }
        } catch (Exception e) {
            log.error("❌ OpenSearch 인덱스 초기화 실패", e);
            // 실패해도 애플리케이션은 계속 실행 (OpenSearch 장애 시에도 서비스 가능)
        }
    }

    /**
     * 인덱스 삭제 (개발/테스트용)
     */
    public void deleteIndex() throws Exception {
        client.indices().delete(d -> d.index(INDEX_NAME));
        log.info("OpenSearch 인덱스 '{}' 삭제 완료", INDEX_NAME);
    }

    /**
     * 인덱스 재생성 (개발/테스트용)
     */
    public void recreateIndex() throws Exception {
        try {
            deleteIndex();
        } catch (Exception e) {
            log.warn("인덱스 삭제 실패 (존재하지 않을 수 있음)");
        }
        initializeIndex();
    }
}

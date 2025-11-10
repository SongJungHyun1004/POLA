package com.jinjinjara.pola.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * OpenSearch 인덱스 초기화 서비스
 * 애플리케이션 시작 시 files 인덱스 존재 여부를 확인합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenSearchIndexService {

    private final OpenSearchClient client;
    private static final String INDEX_NAME = "files";

    /**
     * 애플리케이션 시작 시 인덱스 확인
     *
     * 참고: 인덱스는 OpenSearch Dashboard에서 Nori 매핑으로 수동 생성되어야 합니다.
     * opensearch-mapping.json 파일 참고
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeIndex() {
        try {
            // 인덱스 존재 여부 확인
            boolean exists = client.indices()
                    .exists(ExistsRequest.of(e -> e.index(INDEX_NAME)))
                    .value();

            if (!exists) {
                log.warn("⚠️ OpenSearch 인덱스 '{}' 가 존재하지 않습니다.", INDEX_NAME);
                log.warn("⚠️ OpenSearch Dashboard에서 Nori 매핑으로 인덱스를 생성해주세요.");
                log.warn("⚠️ 참고: opensearch-mapping.json");
            } else {
                log.info("✅ OpenSearch 인덱스 '{}' 확인 완료", INDEX_NAME);
            }
        } catch (Exception e) {
            log.error("❌ OpenSearch 연결 실패", e);
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

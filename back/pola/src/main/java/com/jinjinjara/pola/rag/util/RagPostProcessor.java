package com.jinjinjara.pola.rag.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinjinjara.pola.rag.dto.common.QueryType;
import com.jinjinjara.pola.rag.dto.common.RagSearchSource;
import com.jinjinjara.pola.vision.service.VertexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagPostProcessor {

    private final VertexService vertexService;
    private final QueryClassifier classifier;
    private final PromptFactory promptFactory;
    private final ObjectMapper om = new ObjectMapper();

    // 컨텍스트 제한 (LLM 입력 보호)
    private static final int MAX_CONTEXT_DOCS  = 6;
    private static final int MAX_CONTEXT_CHARS = 3000;


    public String generateAnswer(String userQuery, List<RagSearchSource> sources) {
        try {
            if (sources == null || sources.isEmpty()) {
                return "검색 결과가 없습니다.";
            }

            // 1) 질의 유형 분류
            QueryType type = classifier.classify(userQuery);

            // 2) 상위 스코어 우선 정렬 + 상위 N개만 사용
            List<RagSearchSource> top = sources.stream()
                    .sorted(Comparator.comparing(
                            RagSearchSource::getRelevanceScore,
                            Comparator.nullsLast(Comparator.reverseOrder())
                    ))
                    .limit(MAX_CONTEXT_DOCS)
                    .collect(Collectors.toList());

            // 3) 컨텍스트 조인 (길이 3,000자 컷)
            String contexts = buildContexts(top);

            // 5) 유형별 프롬프트 생성 → LLM 호출
            String prompt = promptFactory.make(type, userQuery, top);

            // (선택) 프롬프트/컨텍스트 길이 로깅
            if (log.isDebugEnabled()) {
                log.debug("[RAG:Post] type={} ctxLen={} promptLen={}",
                        type, contexts.length(), prompt.length());
            }

            String res = vertexService.generateText(prompt,0.2, 512);
            JsonNode node = om.readTree(res);

            if (node.has("candidates")) {
                return node.path("candidates").get(0)
                        .path("content").path("parts").get(0)
                        .path("text").asText();
            }
            return "검색 결과를 요약했습니다.";
        } catch (Exception e) {
            log.error("[RagPostProcessor] LLM 후처리 실패", e);
            return "결과를 생성하는 중 오류가 발생했습니다.";
        }
    }

    /** 상위 스코어 우선, null/공백 제거, 길이 컷 후 조인 */
    private String buildContexts(List<RagSearchSource> sources) {
        String joined = sources.stream()
                .map(RagSearchSource::getContext)
                .filter(Objects::nonNull)
                .map(String::strip)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining("\n---\n"));

        if (joined.length() > MAX_CONTEXT_CHARS) {
            joined = joined.substring(0, MAX_CONTEXT_CHARS) + " …";
        }
        return joined.isEmpty() ? "(검색 결과 없음)" : joined;
    }

}

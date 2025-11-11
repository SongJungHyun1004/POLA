package com.jinjinjara.pola.rag.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinjinjara.pola.rag.dto.common.RagSearchSource;
import com.jinjinjara.pola.vision.service.VertexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagPostProcessor {

    private final VertexService vertexService;
    private final ObjectMapper om = new ObjectMapper();

    public String generateAnswer(String query, List<RagSearchSource> sources) {
        try {
            String joinedContexts = sources.stream()
                    .map(RagSearchSource::getContext)
                    .filter(c -> c != null && !c.isBlank())
                    .limit(6)
                    .reduce((a, b) -> a + "\n---\n" + b)
                    .orElse("(검색 결과 없음)");

            String prompt = """
            너는 한국어 콘텐츠 분석기야.
            사용자의 질문과 관련된 문맥을 요약하거나 답변을 만들어.
            질문 유형에 맞게 자연스럽게 한두 문장으로 응답해.

            [사용자 질문]
            %s

            [검색 결과]
            %s
            """.formatted(query, joinedContexts);

            String res = vertexService.generateTagsFromText(prompt); // Vertex LLM 호출
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
}

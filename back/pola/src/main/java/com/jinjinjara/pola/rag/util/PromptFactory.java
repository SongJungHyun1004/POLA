package com.jinjinjara.pola.rag.util;

import com.jinjinjara.pola.rag.dto.common.QueryType;
import com.jinjinjara.pola.rag.dto.common.RagSearchSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PromptFactory {

    private final RagProperties ragProperties;

    public String make(QueryType type, String userQuery, List<RagSearchSource> sources) {
        int maxDocs = ragProperties.getContext().getMaxDocs();
        int maxChars = ragProperties.getContext().getMaxChars();
        var ctxPolicy = ragProperties.getContext().getPerType().get(type);
        if (ctxPolicy != null) {
            if (ctxPolicy.getMaxDocs() != null)  maxDocs  = ctxPolicy.getMaxDocs();
            if (ctxPolicy.getMaxChars() != null) maxChars = ctxPolicy.getMaxChars();
        }

        final int docsLimit  = Math.max(1, maxDocs);
        final int charsLimit = Math.max(1, maxChars);

        String contexts = sources.stream()
                .map(RagSearchSource::getContext)
                .filter(s -> s != null && !s.isBlank())
                .limit(docsLimit)
                .map(s -> s.length() > charsLimit ? s.substring(0, Math.max(0, charsLimit - 1)) + "…" : s)
                .collect(Collectors.joining("\n---\n"));

        if (type == null) {
            String prompt = """
            너는 한국어 RAG 어시스턴트야.
            아래 네 가지 중 **가장 적절한 하나의 형식**으로만 답해:
            - 한두 문장 요약형 답변
            - 비교 결론 한두 문장
            - 공통점/연관 태그 기반 추천 한두 문장
            - 최근 동향 3줄 이내 + 마지막 줄 간단 추천 1개

            [질문]
            %s

            [문맥]
            %s
            """.formatted(userQuery, contexts);
            log.debug("[PromptFactory] Generated prompt for {}: \n{}", "AUTO", prompt);
            return prompt;
        }

        String prompt = switch (type) {
            case QA -> """
        너는 한국어 요약형 QA 어시스턴트야.
        문맥을 근거로 한두 문장으로 답해. 과장/추측 금지.

        [질문]
        %s

        [문맥]
        %s
        """.formatted(userQuery, contexts);

            case COMPARISON -> """
        너는 비교 분석가야.
        문맥에서 기간/집계치를 추정해 비교 결론을 한두 문장으로 말해.
        수치가 없으면 상대적 표현만 사용.

        [질문]
        %s

        [문맥]
        %s
        """.formatted(userQuery, contexts);

            case COMPOSITE -> """
        너는 추천 생성가야.
        출력 규칙:
        - 정확히 1~2문장, 각 문장 90자 이하(전체 180자 이하)
        - 리스트/표/해시태그/마크다운 금지
        - 질문 금지, 단정형 평서문으로 끝내기

        [질문]
        %s

        [문맥]
        %s
        """.formatted(userQuery, contexts);

            case REPORT -> """
        너는 리포트 생성기야.
        최근 동향을 3줄 이내로 요약하고, 마지막 줄에 간단한 추천 1개를 제시해.

        [질문]
        %s

        [문맥]
        %s
        """.formatted(userQuery, contexts);
        };

        log.debug("[PromptFactory] Generated prompt for {}: \n{}", type, prompt);
        return prompt;
    }
}
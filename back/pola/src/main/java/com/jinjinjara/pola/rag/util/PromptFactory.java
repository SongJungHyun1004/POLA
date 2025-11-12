package com.jinjinjara.pola.rag.util;

import com.jinjinjara.pola.rag.dto.common.QueryType;
import com.jinjinjara.pola.rag.dto.common.RagSearchSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PromptFactory {

    public String make(QueryType type, String userQuery, List<RagSearchSource> sources) {
        String contexts = sources.stream()
                .map(RagSearchSource::getContext)
                .filter(s -> s != null && !s.isBlank())
                .limit(6)
                .collect(Collectors.joining("\n---\n"));

        return switch (type) {
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
                너는 연관성 해석가야.
                상위 결과들의 공통점/연관 태그를 요약해 추천 형태로 제시해.

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
    }
}

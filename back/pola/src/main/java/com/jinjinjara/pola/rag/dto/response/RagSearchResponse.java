package com.jinjinjara.pola.rag.dto.response;

import com.jinjinjara.pola.rag.dto.common.RagSearchSource;

import java.util.List;

public record RagSearchResponse(
        String answer,                 // LLM이 생성한 요약 or 설명
        List<RagSearchSource> sources  // 벡터 검색된 근거 문서들
) {}

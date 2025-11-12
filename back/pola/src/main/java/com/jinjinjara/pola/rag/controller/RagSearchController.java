package com.jinjinjara.pola.rag.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.rag.dto.request.RagSearchRequest;
import com.jinjinjara.pola.rag.dto.response.RagSearchResponse;
import com.jinjinjara.pola.rag.service.RagSearchService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rag")
@RequiredArgsConstructor
@Validated
public class RagSearchController {

    private final RagSearchService ragSearchService;

    @PostMapping("/search")
    public ApiResponse<RagSearchResponse> search(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestBody RagSearchRequest req
    ) {
        RagSearchResponse data = ragSearchService.search(userId, req.query(), 4);
        return ApiResponse.ok(data, "RAG 검색에 성공했습니다.");
    }
}

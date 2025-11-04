package com.jinjinjara.pola.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.search.document.FileDocument;
import com.jinjinjara.pola.search.service.FileSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/files/search")
@RequiredArgsConstructor
public class FileSearchController {

    private final FileSearchService fileSearchService;

    @GetMapping
    public ApiResponse<List<FileDocument>> searchFiles(@RequestParam("q") String keyword) {
        try {
            List<FileDocument> results = fileSearchService.search(keyword);
            return ApiResponse.ok(results, "검색 결과가 성공적으로 조회되었습니다.");
        } catch (Exception e) {
            return ApiResponse.fail("FILE_SEARCH_FAIL", e.getMessage());
        }
    }
}

package com.jinjinjara.pola.controller;

import com.jinjinjara.pola.search.document.FileDocument;
import com.jinjinjara.pola.search.service.FileSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileSearchController {

    private final FileSearchService fileSearchService;

    // 🔍 키워드 기반 통합 검색
    @GetMapping("/search")
    public ResponseEntity<List<FileDocument>> searchFiles(@RequestParam("q") String keyword) {
        List<FileDocument> results = fileSearchService.search(keyword);
        return ResponseEntity.ok(results);
    }
}

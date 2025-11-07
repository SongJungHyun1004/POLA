package com.jinjinjara.pola.vision.controller;


import com.jinjinjara.pola.vision.dto.request.ImageUriRequest;
import com.jinjinjara.pola.vision.dto.request.TagExtractRequest;
import com.jinjinjara.pola.vision.service.VertexService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vertex")
public class VertexController {

    private final VertexService vertexService;

    // 텍스트
    @Operation(summary = "텍스트에서 태그 추출 (JSON 전용)")
    @PostMapping(value = "/text", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> extractTags(@RequestBody TagExtractRequest req) {
        return ResponseEntity.ok(vertexService.generateTagsFromText(req.getText()));
    }

    // 이미지 로컬
    @Operation(summary = "이미지 설명+태그 추출 (멀티모달)")
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> analyzeImage(
            @RequestPart("file") MultipartFile file
    ) throws Exception {
        return ResponseEntity.ok(vertexService.analyzeImage(file.getBytes()));
    }

    // 이미지 URI
    @Operation(summary = "이미지 URL로 설명+태그 추출 (멀티모달)")
    @PostMapping(value = "/url", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> analyzeImageUrl(@RequestBody ImageUriRequest req) throws Exception {
        return ResponseEntity.ok(vertexService.analyzeImageFromUrl(req.getImageUri()));
    }

}
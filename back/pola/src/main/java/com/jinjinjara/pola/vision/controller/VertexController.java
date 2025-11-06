package com.jinjinjara.pola.vision.controller;


import com.jinjinjara.pola.vision.service.VertexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vertex")
public class VertexController {

    private final VertexService vertexService;

    @Operation(summary = "텍스트에서 태그 추출")
    @PostMapping(value = "/tags", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> extractTags(
            @RequestParam(value = "text", required = false) String textFromForm,
            @RequestBody(required = false) Map<String, Object> jsonBody
    ) {
        String text = textFromForm != null ? textFromForm : (jsonBody != null ? (String) jsonBody.get("text") : null);
        return ResponseEntity.ok(vertexService.generateTagsFromText(text));
    }

    @Operation(summary = "이미지 설명+태그 추출")
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> analyzeImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam(name = "hint", required = false) String hint
    ) throws Exception {
        return ResponseEntity.ok(vertexService.analyzeImage(file.getBytes()));
    }
}
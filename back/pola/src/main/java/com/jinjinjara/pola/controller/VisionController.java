package com.jinjinjara.pola.controller;

import com.jinjinjara.pola.vision.dto.response.LabelResponse;
import com.jinjinjara.pola.vision.service.VisionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vision")
public class VisionController {

    private final VisionService visionService;

    @Operation(summary = "라벨 인식 (파일)")
    @PostMapping(value = "/labels", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<LabelResponse>> detectLabels(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "maxResults", required = false) Integer maxResults,
            @RequestParam(value = "lang", required = false) String lang
    ) throws Exception {
        return ResponseEntity.ok(visionService.detectLabels(file, maxResults, lang));
    }

    @Operation(summary = "문서 OCR (파일)")
    @PostMapping(value = "/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> documentOcr(@RequestPart("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(visionService.documentOcr(file));
    }

    @GetMapping("/labels-uri")
    public ResponseEntity<List<LabelResponse>> detectLabelsFromUri(
            @RequestParam("uri") String uri,
            @RequestParam(value = "maxResults", required = false) Integer maxResults,
            @RequestParam(value = "lang", required = false) String lang
    ) throws Exception {
        return ResponseEntity.ok(visionService.detectLabelsFromUri(uri, maxResults, lang));
    }

    @GetMapping("/ocr-uri")
    public ResponseEntity<String> documentOcrFromUri(@RequestParam("uri") String uri) throws Exception {
        return ResponseEntity.ok(visionService.documentOcrFromUri(uri));
    }
}
package com.jinjinjara.pola.vision.controller;

import com.jinjinjara.pola.vision.dto.response.LabelResponse;
import com.jinjinjara.pola.vision.service.EmbeddingService;
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
    private final EmbeddingService embeddingService;

    @Operation(summary = "OCR+임베딩 테스트 (S3 URL → OCR 텍스트 + 임베딩)")
    @GetMapping("/ocr-embed-test") // 예: /api/v1/vision/ocr-embed-test?uri=<presigned_url>
    public ResponseEntity<OcrEmbedResponse> testOcrEmbed(@RequestParam("uri") String uri) {
        String ocrText = visionService.extractTextFromS3Url(uri); // 이미지면 OCR, 텍스트면 그대로
        float[] embedding = embeddingService.embedOcrAndContext(ocrText, ""); // context는 ""로
        return ResponseEntity.ok(new OcrEmbedResponse(ocrText, embedding));
    }

    //추가: 응답 DTO (레코드)
    public record OcrEmbedResponse(String ocrText, float[] embedding) {}

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
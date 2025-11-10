package com.jinjinjara.pola.vision.service;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.jinjinjara.pola.vision.dto.response.LabelResponse;
import com.jinjinjara.pola.vision.util.AIUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VisionService {

    private final ImageAnnotatorClient client;
    private final TranslationService translationService;

    private static final long MAX_IMAGE_BYTES = 20L * 1024 * 1024; // 20MB
    private static final long MAX_TEXT_BYTES  = 2L  * 1024 * 1024; // 2MB

    public String extractTextFromS3Url(String url) {
        try {
            byte[] data = AIUtil.directDownloadBytes(url, MAX_IMAGE_BYTES);
            if (data == null || data.length == 0) {
                throw new RuntimeException("Empty content from S3");
            }

            String mime = AIUtil.sniffMime(data);
            if (mime.startsWith("image/")) {
                return documentOcrFromUri(url);
            }

            if (data.length > MAX_TEXT_BYTES) {
                throw new RuntimeException("Text file too large: " + data.length);
            }

            String text = new String(data, java.nio.charset.StandardCharsets.UTF_8).trim();
            if (text.isBlank()) {
                throw new RuntimeException("Text content empty");
            }
            return text;

        } catch (Exception e) {
            log.error("[VisionService] extractTextFromS3Url failed: {}", e.toString());
            throw new RuntimeException("Failed to extract text from S3 URL", e);
        }
    }

    // -----------------------------
    // 라벨 인식 (파일)
    // -----------------------------
    public List<LabelResponse> detectLabels(MultipartFile file, Integer maxResults) throws Exception {
        Image img = Image.newBuilder()
                .setContent(ByteString.copyFrom(file.getBytes()))
                .build();

        Feature feature = Feature.newBuilder()
                .setType(Feature.Type.LABEL_DETECTION)
                .setMaxResults(maxResults != null ? maxResults : 10)
                .build();

        AnnotateImageRequest req = AnnotateImageRequest.newBuilder()
                .setImage(img)
                .addFeatures(feature)
                .build();

        AnnotateImageResponse r = client.batchAnnotateImages(List.of(req)).getResponses(0);
        if (r.hasError()) throw new RuntimeException(r.getError().getMessage());

        return r.getLabelAnnotationsList().stream()
                .map(a -> new LabelResponse(a.getDescription(), a.getScore()))
                .toList();
    }

    // -----------------------------
    // 라벨 인식 (파일) + 번역
    // lang == "ko" 면 한국어로 변환
    // -----------------------------
    public List<LabelResponse> detectLabels(MultipartFile file, Integer maxResults, String lang) throws Exception {
        List<LabelResponse> labels = detectLabels(file, maxResults);
        return translateLabelsIfNeeded(labels, lang);
    }

    // -----------------------------
    // 라벨 인식 (URI)
    // -----------------------------
    public List<LabelResponse> detectLabelsFromUri(String uri, Integer maxResults) throws Exception {
        ImageSource src = ImageSource.newBuilder().setImageUri(uri).build(); // http(s) 또는 gs://
        Image img = Image.newBuilder().setSource(src).build();

        Feature feature = Feature.newBuilder()
                .setType(Feature.Type.LABEL_DETECTION)
                .setMaxResults(maxResults != null ? maxResults : 10)
                .build();

        AnnotateImageRequest req = AnnotateImageRequest.newBuilder()
                .setImage(img)
                .addFeatures(feature)
                .build();

        AnnotateImageResponse r = client.batchAnnotateImages(List.of(req)).getResponses(0);
        if (r.hasError()) throw new RuntimeException(r.getError().getMessage());

        return r.getLabelAnnotationsList().stream()
                .map(a -> new LabelResponse(a.getDescription(), a.getScore()))
                .toList();
    }

    // -----------------------------
    // 라벨 인식 (URI) + 번역
    // -----------------------------
    public List<LabelResponse> detectLabelsFromUri(String uri, Integer maxResults, String lang) throws Exception {
        List<LabelResponse> labels = detectLabelsFromUri(uri, maxResults);
        return translateLabelsIfNeeded(labels, lang);
    }

    // -----------------------------
    // OCR (파일)
    // -----------------------------
    public String documentOcr(MultipartFile file) throws Exception {
        Image img = Image.newBuilder()
                .setContent(ByteString.copyFrom(file.getBytes()))
                .build();

        Feature feature = Feature.newBuilder()
                .setType(Feature.Type.DOCUMENT_TEXT_DETECTION)
                .build();

        AnnotateImageRequest req = AnnotateImageRequest.newBuilder()
                .setImage(img)
                .addFeatures(feature)
                .build();

        AnnotateImageResponse r = client.batchAnnotateImages(List.of(req)).getResponses(0);
        if (r.hasError()) throw new RuntimeException(r.getError().getMessage());

        return r.getFullTextAnnotation().getText();
    }

    // -----------------------------
    // OCR (URI)
    // -----------------------------
    public String documentOcrFromUri(String uri) throws Exception {
        ImageSource src = ImageSource.newBuilder().setImageUri(uri).build();
        Image img = Image.newBuilder().setSource(src).build();

        Feature feature = Feature.newBuilder()
                .setType(Feature.Type.DOCUMENT_TEXT_DETECTION)
                .build();

        AnnotateImageRequest req = AnnotateImageRequest.newBuilder()
                .setImage(img)
                .addFeatures(feature)
                .build();

        AnnotateImageResponse r = client.batchAnnotateImages(List.of(req)).getResponses(0);
        if (r.hasError()) throw new RuntimeException(r.getError().getMessage());

        return r.getFullTextAnnotation().getText();
    }

    // -----------------------------
    // 내부: 번역 적용 헬퍼
    // 현재 TranslationService는 translateToKo(List<String>) 이므로 lang=="ko"에만 적용
    // -----------------------------
    private List<LabelResponse> translateLabelsIfNeeded(List<LabelResponse> labels, String lang) throws Exception {
        if (lang == null || lang.equalsIgnoreCase("en")) return labels;   // 기본 영문
        if (!lang.equalsIgnoreCase("ko")) return labels;                  // 지금은 ko만 지원

        List<String> originals = labels.stream().map(LabelResponse::getDescription).distinct().toList();
        List<String> translated = translationService.translateToKo(originals);

        Map<String, String> map = zipToMap(originals, translated);

        return labels.stream()
                .map(l -> new LabelResponse(map.getOrDefault(l.getDescription(), l.getDescription()), l.getScore()))
                .toList();
    }

    private static Map<String, String> zipToMap(List<String> keys, List<String> values) {
        return keys.stream().collect(Collectors.toMap(k -> k, k -> values.get(keys.indexOf(k))));
    }
}

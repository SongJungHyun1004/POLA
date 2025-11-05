package com.jinjinjara.pola.vision.service;

import com.google.cloud.translate.v3.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;


@Service
public class TranslationService {

    @Value("${google.project-id}")
    private String projectId;

    @Value("${google.location}")
    private String location;

    private String getParent() {
        return String.format("projects/%s/locations/%s", projectId, location);
    }

    public List<String> translateToKo(List<String> texts) throws Exception {
        try (TranslationServiceClient client = TranslationServiceClient.create()) {
            TranslateTextRequest req = TranslateTextRequest.newBuilder()
                    .setParent(getParent())
                    .setTargetLanguageCode("ko")
                    .addAllContents(texts)
                    .build();

            TranslateTextResponse res = client.translateText(req);

            return res.getTranslationsList().stream()
                    .map(Translation::getTranslatedText)
                    .toList();
        }
    }
}
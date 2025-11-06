package com.jinjinjara.pola.config;

import com.google.api.gax.retrying.RetrySettings;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.threeten.bp.Duration;

@Configuration
public class VisionConfig {

    @Bean
    public ImageAnnotatorClient imageAnnotatorClient() throws Exception {
        // GOOGLE_APPLICATION_CREDENTIALS 가 설정되어 있으면 fromStream 없이 Application Default Credentials로도 동작
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();

        // (선택) 타임아웃/재시도 커스터마이즈
        RetrySettings retry = RetrySettings.newBuilder()
                .setMaxAttempts(5)
                .setInitialRetryDelay(Duration.ofMillis(200))
                .setRetryDelayMultiplier(1.5)
                .setMaxRetryDelay(Duration.ofSeconds(5))
                .setTotalTimeout(Duration.ofSeconds(30))
                .build();

        ImageAnnotatorSettings.Builder builder = ImageAnnotatorSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials));

        builder
                .batchAnnotateImagesSettings()
                .setRetrySettings(retry);

        return ImageAnnotatorClient.create(builder.build());
    }
}
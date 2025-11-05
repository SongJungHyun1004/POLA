package com.jinjinjara.pola.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 자격 증명(쿠키, 인증 헤더) 허용
        config.setAllowCredentials(true);

        // 구체적인 Origin 지정 (allowCredentials: true 일 때 * 사용 불가)
        Arrays.stream(allowedOrigins).forEach(config::addAllowedOrigin);

        // 모든 헤더 허용
        config.addAllowedHeader("*");

        // 모든 HTTP 메서드 허용
        config.addAllowedMethod("*");

        // Preflight 요청 캐싱 (1시간)
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

package com.jinjinjara.pola.config;

import org.apache.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenSearchConfig {

    @Bean
    public OpenSearchClient openSearchClient() {
        RestClient restClient = RestClient.builder(
                new HttpHost("k13d204.p.ssafy.io", 30920, "http")
        ).build();

        return new OpenSearchClient(
                new RestClientTransport(restClient, new JacksonJsonpMapper())
        );
    }
}

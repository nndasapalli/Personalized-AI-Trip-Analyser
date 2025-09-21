package com.easemytrip.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class WebClientHelper {

    private final WebClient.Builder webClientBuilder;

    public WebClientHelper(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public String get(String url) {
        log.info("Sending GET request to URL: {}", url);
        try {
            String response = webClientBuilder.build()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response != null) {
                log.debug("GET response: {}", response);
            } else {
                log.warn("GET request returned null response for URL: {}", url);
            }

            return response;
        } catch (Exception e) {
            log.error("GET request failed for URL '{}': {}", url, e.getMessage(), e);
            return null;
        }
    }

    public String post(String url, String body, String apiKey) {
        log.info("Sending POST request to URL: {}", url);
        log.debug("POST body: {}", body);

        try {
            String response = webClientBuilder.build()
                    .post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response != null) {
                log.debug("POST response: {}", response);
            } else {
                log.warn("POST request returned null response for URL: {}", url);
            }

            return response;
        } catch (Exception e) {
            log.error("POST request failed for URL '{}': {}", url, e.getMessage(), e);
            return null;
        }
    }
}
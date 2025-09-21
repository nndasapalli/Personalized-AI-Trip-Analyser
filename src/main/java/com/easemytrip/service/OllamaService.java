package com.easemytrip.service;

import com.easemytrip.dto.Itinerary;
import com.easemytrip.model.TripRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OllamaService {

    private final WebClientHelper webClientHelper;

    @Value("${Ollama.ApiKey}")
    private String ollamaApiKey;

    public Itinerary generateItinerary(TripRequest request) {
        String prompt = buildPrompt(request);
        String responseContent = callOllamaApi(prompt);
        return buildItinerary(request, responseContent);
    }

    private String buildPrompt(TripRequest request) {
        log.debug("Building prompt...");
        return "Plan a detailed day-wise travel itinerary from "
                + request.getSource() + " to " + request.getDestination()
                + " for the dates " + request.getStartDate() + " to " + request.getEndDate()
                + ". Budget: " + request.getBudget() + " INR."
                + " Include realistic costs in INR and clearly mention travel expenses.";
    }

    private String callOllamaApi(String prompt) {
        log.debug("Preparing request body...");
        String requestBody = "{"
                + "\"model\": \"gpt-oss:120b\","
                + "\"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}],"
                + "\"stream\": false"
                + "}";
        log.debug("Request body: " + requestBody);
        log.debug("calling webClientHelper.post()...");
        return webClientHelper.post("https://ollama.com/api/chat", requestBody, ollamaApiKey);
    }

    private Itinerary buildItinerary(TripRequest request, String content) {
        log.debug("Build Itinerary.....");
        List<String> days = content != null && content.contains("Day 1:") ?
                Arrays.asList(content.split("Day \\d+:")) :
                List.of(content != null ? content : "AI itinerary not available.");

        return Itinerary.builder()
                .source(request.getSource())
                .destination(request.getDestination())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .budget(request.getBudget())
                .days(days)
                .build();
    }
}
package com.easemytrip.service;

import com.easemytrip.dto.WeatherInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WebClientHelper webClientHelper;

    @Value("${openweathermap.api.key}")
    private String owmApiKey;

    public WeatherInfo getWeatherInfo(String city) {
        log.info("Fetching weather info for city: '{}'", city);

        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city
                + "&appid=" + owmApiKey + "&units=metric";

        log.debug("OpenWeatherMap API URL: {}", url);

        String response = webClientHelper.get(url);
        if (response == null || response.isEmpty()) {
            log.warn("Empty response from OpenWeatherMap API for city '{}'", city);
            response = "{}"; // Fallback to empty JSON to prevent parsing errors
        } else {
            log.debug("Received response: {}", response);
        }

        String summary = "Weather info not available";
        try {
            JSONObject json = new JSONObject(response);
            String description = json.getJSONArray("weather").getJSONObject(0).getString("description");
            double temp = json.getJSONObject("main").getDouble("temp");
            summary = description + ", " + temp + "°C";
            log.info("Weather summary for '{}': {}", city, summary);
        } catch (Exception e) {
            log.error("Failed to parse weather info for city '{}'", city, e);
        }

        return WeatherInfo.builder()
                .city(city)
                .forecastSummary(summary)
                .dailyForecasts(List.of(summary))
                .build();
    }
}
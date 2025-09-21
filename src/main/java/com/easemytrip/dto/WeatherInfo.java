package com.easemytrip.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WeatherInfo {
    private String city;
    private String forecastSummary; // e.g., "Sunny, 32°C"
    private List<String> dailyForecasts; // e.g., ["Day 1: Sunny", "Day 2: Rainy"]
}

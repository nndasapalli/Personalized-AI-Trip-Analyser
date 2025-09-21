package com.easemytrip.service;

import com.easemytrip.model.*;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Arrays;


@Service
@RequiredArgsConstructor
public class AIService {
    private final WebClient.Builder webClientBuilder;

    @Value("${openrouteservice.api.key}")
    private String orsApiKey;

    @Value("${openweathermap.api.key}")
    private String owmApiKey;

    @Value("${geoapify.api.key}")
    private String geoapifyApiKey;


    @Value("${opencage.api.key}")
    private String opencageApiKey;



    private WebClient getWebClient() {
        return webClientBuilder.build();
    }

    public PlannerResponse generatePlannerResponse(TripRequest request) {
        System.out.println("Generating planner response for: " + request);
        // 1. AI-generated itinerary from Ollama Phi3
        Itinerary itinerary = callOllama(request);

        // 2. Route info from OpenRouteService
        RouteInfo routeInfo = callOpenRouteService(request.getSource(), request.getDestination());

        // 3. Weather info from OpenWeatherMap
        WeatherInfo weatherInfo = callOpenWeatherMap(request.getDestination());

        // 4. Hospitals nearby from Geoapify
        List<HospitalInfo> hospitals = callGeoapifyHospitals(request.getDestination());

        PlannerResponse response = new PlannerResponse();
        response.setItinerary(itinerary);
        response.setRouteInfo(routeInfo);
        response.setWeatherInfo(weatherInfo);
        response.setNearbyHospitals(hospitals);

        System.out.println("Generated PlannerResponse: " + response);
        return response;
    }

    private Itinerary callOllama(TripRequest request) {
        String prompt = "Plan a detailed day-wise travel itinerary from "
                + request.getSource() + " to " + request.getDestination()
                + " for the dates " + request.getStartDate() + " to " + request.getEndDate()
                + ". Budget: " + request.getBudget() + " INR."
                + " The itinerary should account for and clearly mention the estimated travel expenses for the two-way journey."
                + " All sightseeing, activities, and stays must be planned **only in the destination city (" + request.getDestination() + ").**"
                + " Include realistic costs in Indian Rupees (INR)."
                + " Format output clearly with each day starting as 'Day 1:', 'Day 2:', etc.";

        String ollamaUrl = "http://localhost:11434/api/chat";
        String requestBody = "{"
                + "\"model\": \"phi3\","
                + "\"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}]"
                + "}";

        System.out.println("Calling Ollama Phi3 with prompt: " + prompt);

        StringBuilder contentBuilder = new StringBuilder();
        try {
            List<String> chunks = getWebClient().post()
                    .uri(ollamaUrl)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .collectList()
                    .block();

            for (String jsonChunk : chunks) {
                try {
                    org.json.JSONObject json = new org.json.JSONObject(jsonChunk);
                    if (json.has("message")) {
                        org.json.JSONObject message = json.getJSONObject("message");
                        contentBuilder.append(message.getString("content"));
                    } else if (json.has("content")) {
                        contentBuilder.append(json.getString("content"));
                    }
                } catch (Exception e) {
                    System.out.println("Chunk parse error: " + e.getMessage());
                }
            }
        } catch (Exception ex) {
            System.out.println("Error calling Ollama API: " + ex.getMessage());
            contentBuilder.append("AI itinerary not available. (" + ex.getMessage() + ")");
        }

        String content = contentBuilder.toString();
        System.out.println("Full Ollama itinerary content: " + content);

        List<String> days;
        if (content.contains("Day 1:")) {
            days = Arrays.asList(content.split("Day \\d+:"));
        } else {
            days = Arrays.asList(content);
        }

        Itinerary itinerary = new Itinerary();
        itinerary.setSource(request.getSource());
        itinerary.setDestination(request.getDestination());
        itinerary.setStartDate(request.getStartDate());
        itinerary.setEndDate(request.getEndDate());
        itinerary.setBudget(request.getBudget());
        itinerary.setInterests(request.getInterests());
        itinerary.setAllergies(request.getAllergies());
        itinerary.setMedications(request.getMedications());
        itinerary.setHealthConditions(request.getHealthConditions());
        itinerary.setDays(days);
        return itinerary;
    }

    private RouteInfo callOpenRouteService(String source, String destination) {
        try {

            double[] srcCoords = getCoordinatesFromPlace(source);
            double[] dstCoords = getCoordinatesFromPlace(destination);

            if (srcCoords == null || dstCoords == null) {
                throw new RuntimeException("Unable to fetch coordinates.");
            }

            String orsUrl = "https://api.openrouteservice.org/v2/directions/driving-car?api_key=" + orsApiKey
                    + "&start=" + srcCoords[1] + "," + srcCoords[0]
                    + "&end=" + dstCoords[1] + "," + dstCoords[0];

            System.out.println("Calling OpenRouteService with URL: " + orsUrl);

            String responseBody = getWebClient().get()
                    .uri(orsUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("OpenRouteService raw response: " + responseBody);

            String distance = "";
            String duration = "";
            try {
                org.json.JSONObject json = new org.json.JSONObject(responseBody);
                org.json.JSONArray features = json.getJSONArray("features");
                if (features.length() > 0) {
                    org.json.JSONObject properties = features.getJSONObject(0).getJSONObject("properties");
                    distance += properties.getJSONObject("summary").getDouble("distance");
                    duration += properties.getJSONObject("summary").getDouble("duration");
                }
            } catch (Exception e) {
                System.out.println("Error parsing OpenRouteService response: " + e.getMessage());
            }

            RouteInfo routeInfo = new RouteInfo();
            routeInfo.setSource(source);
            routeInfo.setDestination(destination);
            routeInfo.setDistance(distance);
            routeInfo.setDuration(duration);
            routeInfo.setRouteMapUrl(orsUrl);

            System.out.println("Final RouteInfo object: " + routeInfo);
            return routeInfo;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private WeatherInfo callOpenWeatherMap(String city) {
        String owmUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + owmApiKey + "&units=metric";

        System.out.println("Calling OpenWeatherMap with URL: " + owmUrl);

        String responseBody = getWebClient().get()
                .uri(owmUrl)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("OpenWeatherMap raw response: " + responseBody);

        String summary = "";
        try {
            org.json.JSONObject json = new org.json.JSONObject(responseBody);
            summary = json.getJSONArray("weather").getJSONObject(0).getString("description")
                    + ", " + json.getJSONObject("main").get("temp") + "°C";
        } catch (Exception e) {
            System.out.println("Error parsing OpenWeatherMap response: " + e.getMessage());
            summary = "Weather info not available";
        }

        WeatherInfo weatherInfo = new WeatherInfo();
        weatherInfo.setCity(city);
        weatherInfo.setForecastSummary(summary);
        weatherInfo.setDailyForecasts(Arrays.asList(summary));
        System.out.println("Final WeatherInfo object: " + weatherInfo);
        return weatherInfo;
    }

    private List<HospitalInfo> callGeoapifyHospitals(String city) {
        double[] cityCoords = getCoordinatesFromPlace(city);
        if (cityCoords == null) {
            System.out.println("Could not get coordinates for " + city + ", returning empty hospital list.");
            return List.of();
        }
        double lat = cityCoords[0];
        double lon = cityCoords[1];

        String geoapifyUrl = "https://api.geoapify.com/v2/places?categories=healthcare.hospital"
                + "&filter=circle:" + lon + "," + lat + ",5000"
                + "&limit=5"
                + "&apiKey=" + geoapifyApiKey;

        System.out.println("Calling Geoapify with URL: " + geoapifyUrl);

        String responseBody = getWebClient().get()
                .uri(geoapifyUrl)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("Geoapify raw response: " + responseBody);

        List<HospitalInfo> hospitals = new java.util.ArrayList<>();
        try {
            org.json.JSONObject json = new org.json.JSONObject(responseBody);
            org.json.JSONArray features = json.getJSONArray("features");
            for (int i = 0; i < features.length(); i++) {
                org.json.JSONObject prop = features.getJSONObject(i).getJSONObject("properties");
                HospitalInfo hospital = new HospitalInfo();
                hospital.setName(prop.optString("name", "Unknown Hospital"));
                hospital.setAddress(prop.optString("address_line1", "No address"));
                hospital.setContact(prop.optString("phone", "No contact"));
                hospital.setLatitude(features.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates").getDouble(1));
                hospital.setLongitude(features.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates").getDouble(0));
                hospitals.add(hospital);
            }
        } catch (Exception e) {
            System.out.println("Error parsing Geoapify response: " + e.getMessage());
            // fallback: empty list
        }
        System.out.println("Final hospital list: " + hospitals);
        return hospitals;
    }

    private double[] getCoordinatesFromPlace(String place) {
        try {
            String encodedPlace = URLEncoder.encode(place, StandardCharsets.UTF_8);
            String apiKey = opencageApiKey; // Use injected API key

            String url = "https://api.opencagedata.com/geocode/v1/json?q=" + encodedPlace + "&key=" + apiKey;

            String response = getWebClient().get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JSONObject json = new JSONObject(response);
            JSONArray results = json.getJSONArray("results");
            if (results.length() > 0) {
                JSONObject geometry = results.getJSONObject(0).getJSONObject("geometry");
                double lat = geometry.getDouble("lat");
                double lng = geometry.getDouble("lng");
                return new double[]{lat, lng};
            } else {
                System.out.println("No results found for place: " + place);
            }
        } catch (Exception e) {
            System.out.println("Failed to geocode '" + place + "': " + e.getMessage());
        }
        return null;
    }

}

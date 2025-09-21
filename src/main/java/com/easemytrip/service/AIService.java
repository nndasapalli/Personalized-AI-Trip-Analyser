package com.easemytrip.service;

import com.easemytrip.dto.*;
import com.easemytrip.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final OllamaService ollamaService;
    private final RouteService routeService;
    private final WeatherService weatherService;
    private final HospitalService hospitalService;

    public PlannerResponse generatePlannerResponse(TripRequest request) {
        log.info("Generating planner response for trip: {} -> {}", request.getSource(), request.getDestination());

        Itinerary itinerary = ollamaService.generateItinerary(request);
        log.debug("Generated itinerary: {}", itinerary);

        RouteInfo routeInfo = routeService.getRouteInfo(request.getSource(), request.getDestination());
        log.debug("Route info: {}", routeInfo);

        WeatherInfo weatherInfo = weatherService.getWeatherInfo(request.getDestination());
        log.debug("Weather info: {}", weatherInfo);

        List<HospitalInfo> hospitals = hospitalService.getNearbyHospitals(request.getDestination());
        log.debug("Nearby hospitals: {}", hospitals);

        PlannerResponse response = PlannerResponse.builder()
                .itinerary(itinerary)
                .routeInfo(routeInfo)
                .weatherInfo(weatherInfo)
                .nearbyHospitals(hospitals)
                .build();

        log.info("Planner response successfully generated for {}", request.getDestination());
        return response;
    }
}
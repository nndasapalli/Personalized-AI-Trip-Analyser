package com.easemytrip.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PlannerResponse {
    private Itinerary itinerary;
    private RouteInfo routeInfo;
    private WeatherInfo weatherInfo;
    private List<HospitalInfo> nearbyHospitals;
}
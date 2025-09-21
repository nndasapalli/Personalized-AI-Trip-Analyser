package com.easemytrip.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlannerResponse {
    private Itinerary itinerary;
    private RouteInfo routeInfo;
    private WeatherInfo weatherInfo;
    private List<HospitalInfo> nearbyHospitals;
}
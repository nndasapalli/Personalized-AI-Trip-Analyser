package com.easemytrip.service;

import com.easemytrip.dto.RouteInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {

    private final WebClientHelper webClientHelper;
    private final GeoCodingService geoCodingService;

    @Value("${openrouteservice.api.key}")
    private String orsApiKey;

//    public RouteInfo getRouteInfo(String source, String destination) {
//        double[] srcCoords = geoCodingService.getCoordinates(source);
//        double[] dstCoords = geoCodingService.getCoordinates(destination);
//
//        if (srcCoords == null || dstCoords == null) return null;
//
//
//        String orsUrl = "https://api.openrouteservice.org/v2/directions/driving-car?api_key=" + orsApiKey
//                + "&start=" + srcCoords[1] + "," + srcCoords[0]
//                + "&end=" + dstCoords[1] + "," + dstCoords[0];
//
//        log.debug("Getting Source and destination coordinates....");
//        String response = webClientHelper.get(orsUrl);
//        String distance = "", duration = "";
//
//        try {
//            JSONObject json = new JSONObject(response);
//            JSONArray features = json.getJSONArray("features");
//            if (features.length() > 0) {
//                JSONObject summary = features.getJSONObject(0).getJSONObject("properties").getJSONObject("summary");
//                distance = String.valueOf(summary.getDouble("distance"));
//                duration = String.valueOf(summary.getDouble("duration"));
//            }
//        } catch (Exception ignored) {}
//
//        return RouteInfo.builder()
//                .source(source)
//                .destination(destination)
//                .distance(distance)
//                .duration(duration)
//                .routeMapUrl(orsUrl)
//                .build();
//    }
public RouteInfo getRouteInfo(String source, String destination) {
    // Get coordinates for source and destination
    double[] srcCoords = geoCodingService.getCoordinates(source);
    double[] dstCoords = geoCodingService.getCoordinates(destination);

    if (srcCoords == null || dstCoords == null) {
        log.warn("Coordinates not found for source '{}' or destination '{}'", source, destination);
        return null; // Or consider throwing a custom exception
    }

    // Build ORS URL safely
    String orsUrl = UriComponentsBuilder
            .fromHttpUrl("https://api.openrouteservice.org/v2/directions/driving-car")
            .queryParam("api_key", orsApiKey)
            .queryParam("start", srcCoords[1] + "," + srcCoords[0])
            .queryParam("end", dstCoords[1] + "," + dstCoords[0])
            .toUriString();

    log.debug("Fetching route info from ORS API: {}", orsUrl);

    String response;
    try {
        response = webClientHelper.get(orsUrl);
    } catch (Exception e) {
        log.error("Error while calling ORS API", e);
        return null;
    }

    double distanceKm = 0;
    double durationMin = 0;

    try {
        JSONObject json = new JSONObject(response);
        JSONArray features = json.getJSONArray("features");

        if (features.length() > 0) {
            JSONObject summary = features.getJSONObject(0)
                    .getJSONObject("properties")
                    .getJSONObject("summary");

            distanceKm = summary.getDouble("distance") / 1000.0; // meters → km
            durationMin = summary.getDouble("duration") / 60.0;   // seconds → minutes
        }
    } catch (Exception e) {
        log.error("Failed to parse ORS response: {}", response, e);
    }

    return RouteInfo.builder()
            .source(source)
            .destination(destination)
            .distance(String.format("%.2f km", distanceKm))
            .duration(String.format("%.2f mins", durationMin))
            .routeMapUrl(orsUrl)
            .build();
}

}
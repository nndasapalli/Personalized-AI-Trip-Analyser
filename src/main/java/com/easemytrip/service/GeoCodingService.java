package com.easemytrip.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoCodingService {

    private final WebClientHelper webClientHelper;

    @Value("${opencage.api.key}")
    private String opencageApiKey;

    public double[] getCoordinates(String place) {
        log.info("Fetching coordinates for place: '{}'", place);

        try {
            String encodedPlace = URLEncoder.encode(place, StandardCharsets.UTF_8);
            String url = "https://api.opencagedata.com/geocode/v1/json?q=" + encodedPlace + "&key=" + opencageApiKey;

            log.debug("Encoded place: '{}', URL: '{}'", encodedPlace, url);

            String response = webClientHelper.get(url);

            if (response != null) {
                log.debug("Received response: {}", response);

                JSONObject json = new JSONObject(response);
                JSONArray results = json.getJSONArray("results");

                if (results.length() > 0) {
                    JSONObject geometry = results.getJSONObject(0).getJSONObject("geometry");
                    double lat = geometry.getDouble("lat");
                    double lng = geometry.getDouble("lng");
                    log.info("Coordinates for '{}' -> lat: {}, lng: {}", place, lat, lng);
                    return new double[]{lat, lng};
                } else {
                    log.warn("No results found for place '{}'", place);
                }
            } else {
                log.warn("Empty response from geocoding API for place '{}'", place);
            }
        } catch (Exception e) {
            log.error("Failed to geocode '{}'", place, e);
        }

        return null;
    }
}
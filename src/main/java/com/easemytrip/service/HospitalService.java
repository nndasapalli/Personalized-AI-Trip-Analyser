package com.easemytrip.service;

import com.easemytrip.dto.HospitalInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HospitalService {

    private final WebClientHelper webClientHelper;
    private final GeoCodingService geoCodingService;

    @Value("${geoapify.api.key}")
    private String geoapifyApiKey;

    public List<HospitalInfo> getNearbyHospitals(String city) {
        log.info("Fetching nearby hospitals for city: '{}'", city);

        double[] coords = geoCodingService.getCoordinates(city);
        if (coords == null) {
            log.warn("Coordinates not found for city '{}', returning empty hospital list.", city);
            return List.of();
        }

        String url = "https://api.geoapify.com/v2/places?categories=healthcare.hospital"
                + "&filter=circle:" + coords[1] + "," + coords[0] + ",5000"
                + "&limit=5"
                + "&apiKey=" + geoapifyApiKey;

        log.debug("Geoapify API URL: {}", url);

        String response = webClientHelper.get(url);
        if (response == null || response.isEmpty()) {
            log.warn("Empty response from Geoapify API for city '{}'", city);
            return List.of();
        }

        List<HospitalInfo> hospitals = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(response);
            JSONArray features = json.getJSONArray("features");

            log.info("Found {} hospitals in response for city '{}'", features.length(), city);

            for (int i = 0; i < features.length(); i++) {
                JSONObject feature = features.getJSONObject(i);
                JSONObject prop = feature.getJSONObject("properties");
                JSONArray coordsArr = feature.getJSONObject("geometry").getJSONArray("coordinates");

                HospitalInfo hospital = HospitalInfo.builder()
                        .name(prop.optString("name", "Unknown Hospital"))
                        .address(prop.optString("address_line1", "No address"))
                        .contact(prop.optString("phone", "No contact"))
                        .longitude(coordsArr.getDouble(0))
                        .latitude(coordsArr.getDouble(1))
                        .build();

                log.debug("Hospital {}: {}", i + 1, hospital);
                hospitals.add(hospital);
            }
        } catch (Exception e) {
            log.error("Failed to parse hospitals response for city '{}'", city, e);
        }

        return hospitals;
    }
}
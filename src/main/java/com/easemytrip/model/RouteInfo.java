package com.easemytrip.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RouteInfo {
    private String source;
    private String destination;
    private String distance; // e.g., "600 km"
    private String duration; // e.g., "8 hours"
    private String routeMapUrl; // URL to map image or directions
}

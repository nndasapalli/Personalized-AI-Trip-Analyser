package com.easemytrip.model;

import lombok.Data;

@Data
public class TripRequest {
    private String source;
    private String destination;
    private String startDate;
    private String endDate;
    private String budget;
    private String interests;
    private String allergies;
    private String medications;
    private String healthConditions;
}

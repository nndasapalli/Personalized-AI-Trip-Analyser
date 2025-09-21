package com.easemytrip.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
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

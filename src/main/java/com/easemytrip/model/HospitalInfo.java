package com.easemytrip.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HospitalInfo {
    private String name;
    private String address;
    private String contact;
    private double latitude;
    private double longitude;
}
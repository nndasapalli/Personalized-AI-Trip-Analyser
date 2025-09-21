package com.easemytrip.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HospitalInfo {
    private String name;
    private String address;
    private String contact;
    private double latitude;
    private double longitude;
}
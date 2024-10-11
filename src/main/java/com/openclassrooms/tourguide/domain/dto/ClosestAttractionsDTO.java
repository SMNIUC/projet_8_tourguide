package com.openclassrooms.tourguide.domain.dto;

import gpsUtil.location.Location;
import lombok.Data;

@Data
public class ClosestAttractionsDTO
{
    private String attractionName;
    private Location attractionLocation;
    private Location userLocation;
    private Double userDistanceInMilesToAttraction;
    private int rewardPoints;
}

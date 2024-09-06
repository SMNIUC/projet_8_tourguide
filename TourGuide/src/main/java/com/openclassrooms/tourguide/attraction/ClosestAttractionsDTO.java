package com.openclassrooms.tourguide.attraction;

import gpsUtil.location.Location;
import lombok.Data;

@Data
public class ClosestAttractionsDTO
{
    private String attractionName;
    private Location attractionLocation;
    private Location userLocation;
    private Double userDistanceToAttraction;
    private int rewardPoints;
}

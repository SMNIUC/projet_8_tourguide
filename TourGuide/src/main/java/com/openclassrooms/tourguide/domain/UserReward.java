package com.openclassrooms.tourguide.domain;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import lombok.Data;

@Data
public class UserReward
{
    public final VisitedLocation visitedLocation;
    public final Attraction 	 attraction;
    public final int 			 rewardPoints;
}

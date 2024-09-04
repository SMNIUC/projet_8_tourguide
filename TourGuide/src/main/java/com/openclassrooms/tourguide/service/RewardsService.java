package com.openclassrooms.tourguide.service;

import java.util.List;

import lombok.Data;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

@Data
@Service
public class RewardsService
{
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    // proximity in miles
    private int defaultProximityBuffer 	 = 10;
    private int proximityBuffer 		 = defaultProximityBuffer;
    private int attractionProximityRange = 200;

    private final GpsUtil gpsUtil;
    private final RewardCentral rewardsCentral;

    public RewardsService( GpsUtil gpsUtil, RewardCentral rewardCentral )
    {
        this.gpsUtil = gpsUtil;
        this.rewardsCentral = rewardCentral;
    }

    public void setDefaultProximityBuffer( )
    {
        proximityBuffer = defaultProximityBuffer;
    }

    public void calculateRewards( User user )
    {
        List<VisitedLocation> userLocations = user.getVisitedLocations( );
        List<Attraction> attractions = gpsUtil.getAttractions( );

        for ( VisitedLocation visitedLocation : userLocations )
        {
            for ( Attraction attraction : attractions )
            {
                // This line compares all the already existing userRewards to ensure none matches those from the attractions near the visited locations
                if ( user.getUserRewards( ).stream( ).noneMatch( r -> r.attraction.attractionName.equals( attraction.attractionName ) ) )
                {
                    // If none matches, and if the locations are less than 10 miles apart...
                    if ( nearAttraction( visitedLocation, attraction ) )
                    {
                        // create a new reward and add it to the user's rewards list
                        user.addUserReward( new UserReward( visitedLocation, attraction, getRewardPoints( attraction, user ) ) );
                    }
                }
            }
        }
    }

    public boolean isWithinAttractionProximity( Attraction attraction, Location location )
    {
        return !( getDistance( attraction, location ) > attractionProximityRange );
    }

    private boolean nearAttraction( VisitedLocation visitedLocation, Attraction attraction )
    {
        // returns True if the attraction is closer than 10 miles (included) from the visited location
        return !( getDistance( attraction, visitedLocation.location ) > proximityBuffer );
    }

    private int getRewardPoints( Attraction attraction, User user )
    {
        return rewardsCentral.getAttractionRewardPoints( attraction.attractionId, user.getUserId( ) );
    }

    public double getDistance( Location loc1, Location loc2 )
    {
        double lat1 = Math.toRadians( loc1.latitude );
        double lon1 = Math.toRadians( loc1.longitude );
        double lat2 = Math.toRadians( loc2.latitude );
        double lon2 = Math.toRadians( loc2.longitude );

        double angle = Math.acos( Math.sin( lat1 ) * Math.sin( lat2 )
                + Math.cos( lat1 ) * Math.cos( lat2 ) * Math.cos( lon1 - lon2 ) );

        double nauticalMiles = 60 * Math.toDegrees( angle );
        return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
    }
}

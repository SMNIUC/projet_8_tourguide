package com.openclassrooms.tourguide.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.Data;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.domain.User;
import com.openclassrooms.tourguide.domain.UserReward;

/**
 * Service for calculating rewards for users based on their proximity to attractions.
 *
 * <p>
 * This service calculates rewards for users by determining whether they have visited any nearby
 * attractions and assigns reward points accordingly. It utilizes GPS data to determine user locations
 * and calculate distances between users and attractions.
 * </p>
 */
@Data
@Service
public class RewardsService
{
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    // Proximity buffers and ranges
    private int defaultProximityBuffer 	 = 10;
    private int proximityBuffer 		 = defaultProximityBuffer;
    private int attractionProximityRange = 200;

    // External services for GPS and rewards management
    private final GpsUtil gpsUtil;
    private final RewardCentral rewardsCentral;

    /**
     * Constructs a new {@code RewardsService} with the specified GPS and rewards services.
     *
     * @param gpsUtil        the {@link GpsUtil} service used to retrieve GPS data
     * @param rewardCentral  the {@link RewardCentral} service used to manage reward points
     */
    public RewardsService( GpsUtil gpsUtil, RewardCentral rewardCentral )
    {
        this.gpsUtil = gpsUtil;
        this.rewardsCentral = rewardCentral;
    }

    /**
     * Resets the proximity buffer to its default value.
     *
     * <p>This method resets the proximity buffer used in distance calculations
     * back to the default proximity buffer value.</p>
     */
    public void setDefaultProximityBuffer( )
    {
        proximityBuffer = defaultProximityBuffer;
    }

    /**
     * Calculates rewards for a given user by comparing the new visited location with known attractions.
     *
     * <p>If a user has visited an attraction within the proximity range and has not already been rewarded
     * for that attraction, this method will calculate and assign reward points.</p>
     *
     * @param user the {@link User} for whom rewards are to be calculated
     */
    public void calculateRewards( User user )
    {
        List<VisitedLocation> visitedLocations = user.getVisitedLocations( );
        List<Attraction> attractions = gpsUtil.getAttractions( );
        CopyOnWriteArrayList<UserReward> originalUserRewardsList = new CopyOnWriteArrayList<>( user.getUserRewards( ) );

        for( VisitedLocation visitedLocation : visitedLocations )
        {
            for ( Attraction attraction : attractions )
            {
                // This line compares all the already existing userRewards to ensure none matches those from the attractions near the visited locations
                // Looping through the userRewards stream while I'm updating it two lines below....
                if ( originalUserRewardsList.stream( ).noneMatch( r -> r.attraction.attractionName.equals( attraction.attractionName ) ) )
                {
                    // If none matches, and if the locations are less than 10 miles apart...
                    if ( nearAttraction( visitedLocation, attraction ) )
                    {
                        // create a new reward and add it to the user's rewards list
                        originalUserRewardsList.add( new UserReward( visitedLocation, attraction, getRewardPoints( attraction, user ) ) );
                    }
                }
            }
        }
        user.setUserRewards( originalUserRewardsList );
    }

    /**
     * Checks if a given location is within proximity to a specified attraction.
     *
     * <p>This method determines if a user's location is within the defined attraction proximity range.</p>
     *
     * @param attraction the {@link Attraction} to compare against
     * @param location   the {@link Location} of the user
     * @return {@code true} if the location is within the proximity range, {@code false} otherwise
     */
    public boolean isWithinAttractionProximity( Attraction attraction, Location location )
    {
        return !( getDistance( attraction, location ) > attractionProximityRange );
    }

    /**
     * Checks if a visited location is near a specified attraction using the proximity buffer.
     *
     * @param visitedLocation the {@link VisitedLocation} of the user
     * @param attraction      the {@link Attraction} to compare against
     * @return {@code true} if the visited location is within the proximity buffer, {@code false} otherwise
     */
    private boolean nearAttraction( VisitedLocation visitedLocation, Attraction attraction )
    {
        // returns True if the attraction is closer than 10 miles (included) from the visited location
        return !( getDistance( attraction, visitedLocation.location ) > proximityBuffer );
    }

    /**
     * Retrieves the reward points for visiting a specified attraction.
     *
     * @param attraction the {@link Attraction} for which to retrieve reward points
     * @param user       the {@link User} who visited the attraction
     * @return the number of reward points earned for visiting the attraction
     */
    private int getRewardPoints( Attraction attraction, User user )
    {
        return rewardsCentral.getAttractionRewardPoints( attraction.attractionId, user.getUserId( ) );
    }

    /**
     * Calculates the distance in statute miles between two locations.
     *
     * <p>This method uses the Haversine formula to calculate the distance between two geographic
     * locations, taking into account the curvature of the Earth.</p>
     *
     * @param loc1 the first {@link Location}
     * @param loc2 the second {@link Location}
     * @return the distance between the two locations in statute miles
     */
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

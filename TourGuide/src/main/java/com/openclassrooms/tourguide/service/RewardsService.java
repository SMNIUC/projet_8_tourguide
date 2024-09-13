package com.openclassrooms.tourguide.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
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
@Service
@RequiredArgsConstructor
public class RewardsService
{
    // External services for GPS and rewards management
    private final GpsUtil gpsUtil;
    private final RewardCentral rewardsCentral;

    private final LocationService locationService;


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
                    if ( locationService.nearAttraction( visitedLocation, attraction ) )
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
}

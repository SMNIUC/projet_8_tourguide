package com.openclassrooms.tourguide.service;

import java.util.List;
import java.util.concurrent.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.domain.User;
import com.openclassrooms.tourguide.domain.UserReward;

/**
 * The {@code RewardsService} class provides functionality for calculating and assigning rewards to users based on
 * their visits to attractions. It compares users' visited locations with known attractions, determines proximity,
 * and calculates reward points when a visit qualifies.
 *
 * <p>This service integrates the following external utilities:
 * <ul>
 *     <li>{@code GpsUtil} - retrieves attraction data for reward calculation</li>
 *     <li>{@code RewardCentral} - calculates the reward points for visiting attractions</li>
 *     <li>{@code LocationService} - determines proximity between users' locations and attractions</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class RewardsService
{
    // External services for GPS and rewards management
    private final GpsUtil gpsUtil;
    private final RewardCentral rewardsCentral;

    private final LocationService locationService;
    ExecutorService executorService = Executors.newCachedThreadPool( );

    /**
     * Calculates rewards for a given user by comparing the user's new visited location with known attractions.
     *
     * <p>If a user has visited an attraction within the proximity range and has not already been rewarded
     * for that attraction, this method calculates and assigns reward points.</p>
     *
     * @param user the {@link User} for whom rewards are to be calculated
     */
    public void calculateRewards( User user )
    {
        List<VisitedLocation> visitedLocations = user.getVisitedLocations( );
        List<Attraction> attractions = gpsUtil.getAttractions( );

        // Get the current user rewards
        CopyOnWriteArrayList<UserReward> originalUserRewardsList = new CopyOnWriteArrayList<>( user.getUserRewards( ) );

        visitedLocations.forEach( visitedLocation ->
            attractions.forEach( attraction -> {
                // Check if there's no existing reward for the attraction
                boolean noExistingRewards = originalUserRewardsList.stream( )
                        .noneMatch( r -> r.attraction.attractionName.equals( attraction.attractionName ) );

                // If no reward exists and the attraction is near the visited location
                if ( noExistingRewards && locationService.nearAttraction( visitedLocation, attraction ) ) {
                    // Add new reward
                    UserReward reward = new UserReward( visitedLocation, attraction );
                    calculateRewardPoints( attraction, user, reward );
                    originalUserRewardsList.add( reward );
                }
            } )
        );
        user.setUserRewards( originalUserRewardsList );
    }


    /**
     * Retrieves the reward points for visiting a specified attraction and assigns them to a reward.
     * The points are asynchronously fetched from {@code RewardCentral}.
     *
     * @param attraction the {@link Attraction} for which to retrieve reward points
     * @param user       the {@link User} who visited the attraction
     * @param reward     the {@link UserReward} object to store the calculated reward points
     */
    public void calculateRewardPoints( Attraction attraction, User user, UserReward reward )
    {
        CompletableFuture.supplyAsync( () -> rewardsCentral.getAttractionRewardPoints( attraction.attractionId, user.getUserId( ) ), executorService )
                .thenAccept( reward::setRewardPoints );
    }
}
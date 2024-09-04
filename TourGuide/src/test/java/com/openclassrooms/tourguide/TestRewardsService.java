package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

public class TestRewardsService
{
    @Test
    public void userGetRewards( )
    {
        GpsUtil gpsUtil = new GpsUtil( );
        RewardsService rewardsService = new RewardsService( gpsUtil, new RewardCentral( ) );

        InternalTestHelper.setInternalUserNumber( 0 );
        TourGuideService tourGuideService = new TourGuideService( gpsUtil, rewardsService );

        User user = new User( UUID.randomUUID( ), "jon", "000", "jon@tourGuide.com" );
        Attraction attraction = gpsUtil.getAttractions( ).get( 0 );
        user.addToVisitedLocations( new VisitedLocation( user.getUserId( ), attraction, new Date( ) ) );
        tourGuideService.trackUserLocation( user );
        List<UserReward> userRewards = user.getUserRewards( );
        tourGuideService.tracker.stopTracking( );
        assertEquals( 1, userRewards.size( ) );
    }

    @Test
    public void isWithinAttractionProximity( )
    {
        GpsUtil gpsUtil = new GpsUtil( );
        RewardsService rewardsService = new RewardsService( gpsUtil, new RewardCentral( ) );
        Attraction attraction = gpsUtil.getAttractions( ).get( 0 );
        assertTrue( rewardsService.isWithinAttractionProximity( attraction, attraction ) );
    }

//    @Disabled // Needs fixed - can throw ConcurrentModificationException
    @Test
    public void nearAllAttractions( )
    {
        //GIVEN
        GpsUtil gpsUtil = new GpsUtil( );
        RewardsService rewardsService = new RewardsService( gpsUtil, new RewardCentral( ) );
        rewardsService.setProximityBuffer( Integer.MAX_VALUE );

        InternalTestHelper.setInternalUserNumber( 1 );
        TourGuideService tourGuideService = new TourGuideService( gpsUtil, rewardsService );
        User user = tourGuideService.getAllUsers( ).get( 0 );

        // WHEN
        rewardsService.calculateRewards( user );
        List<UserReward> userRewards = tourGuideService.getUserRewards( user );
        tourGuideService.tracker.stopTracking( );

        // THEN
        assertEquals( gpsUtil.getAttractions( ).size( ), userRewards.size( ) );
    }
}
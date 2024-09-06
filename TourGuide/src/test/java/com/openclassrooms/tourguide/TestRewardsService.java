package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.openclassrooms.tourguide.service.LocationService;
import com.openclassrooms.tourguide.service.test.TestingService;
import com.openclassrooms.tourguide.service.UserService;
import org.junit.jupiter.api.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.testUtils.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.domain.User;
import com.openclassrooms.tourguide.domain.UserReward;

public class TestRewardsService
{
    @Test
    public void userGetRewards( )
    {
        GpsUtil gpsUtil = new GpsUtil( );
        RewardsService rewardsService = new RewardsService( gpsUtil, new RewardCentral( ) );

        InternalTestHelper.setInternalUserNumber( 0 );
        TestingService testingService = new TestingService( );
        LocationService locationService = new LocationService( gpsUtil, rewardsService );
        UserService userService = new UserService( testingService, locationService );

        User user = new User( UUID.randomUUID( ), "jon", "000", "jon@tourGuide.com" );
        Attraction attraction = gpsUtil.getAttractions( ).get( 0 );
        user.addToVisitedLocations( new VisitedLocation( user.getUserId( ), attraction, new Date( ) ) );
        locationService.trackUserLocation( user );
        List<UserReward> userRewards = user.getUserRewards( );
        userService.tracker.stopTracking( );
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

    @Test
    public void nearAllAttractions( )
    {
        //GIVEN
        GpsUtil gpsUtil = new GpsUtil( );
        RewardsService rewardsService = new RewardsService( gpsUtil, new RewardCentral( ) );
        rewardsService.setProximityBuffer( Integer.MAX_VALUE );

        InternalTestHelper.setInternalUserNumber( 1 );
        TestingService testingService = new TestingService( );
        LocationService locationService = new LocationService( gpsUtil, rewardsService );
        UserService userService = new UserService( testingService, locationService );
        User user = userService.getAllUsers( ).get( 0 );

        // WHEN
        rewardsService.calculateRewards( user );
        List<UserReward> userRewards = user.getUserRewards( );
        userService.tracker.stopTracking( );

        // THEN
        assertEquals( gpsUtil.getAttractions( ).size( ), userRewards.size( ) );
    }
}



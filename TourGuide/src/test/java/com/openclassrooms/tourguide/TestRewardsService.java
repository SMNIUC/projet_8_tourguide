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
        // GIVEN
        GpsUtil gpsUtil = new GpsUtil( );
        TestingService testingService = new TestingService( );
        RewardsService rewardsService = new RewardsService( gpsUtil, new RewardCentral( ), new LocationService( new RewardCentral( ), gpsUtil ) );

        InternalTestHelper.setInternalUserNumber( 0 );
        UserService userService = new UserService( gpsUtil, rewardsService, testingService );

        // WHEN
        User user = new User( UUID.randomUUID( ), "jon", "000", "jon@tourGuide.com" );
        Attraction attraction = gpsUtil.getAttractions( ).get( 0 );
        user.addToVisitedLocations( new VisitedLocation( user.getUserId( ), attraction, new Date( ) ) );
        userService.trackUserLocation( user );
        List<UserReward> userRewards = user.getUserRewards( );
        userService.tracker.stopTracking( );

        // THEN
        assertEquals( 1, userRewards.size( ) );
    }

    @Test
    public void isWithinAttractionProximity( )
    {
        // GIVEN
        GpsUtil gpsUtil = new GpsUtil( );
        RewardCentral rewardCentral = new RewardCentral( );
        LocationService locationService = new LocationService( rewardCentral, gpsUtil );

        // WHEN
        Attraction attraction = gpsUtil.getAttractions( ).get( 0 );

        // THEN
        assertTrue( locationService.isWithinAttractionProximity( attraction, attraction ) );
    }

    @Test
    public void nearAllAttractions( )
    {
        //GIVEN
        GpsUtil gpsUtil = new GpsUtil( );
        TestingService testingService = new TestingService( );
        RewardCentral rewardCentral = new RewardCentral( );
        LocationService locationService = new LocationService( rewardCentral, gpsUtil );
        locationService.setProximityBuffer( Integer.MAX_VALUE );

        RewardsService rewardsService = new RewardsService( gpsUtil, new RewardCentral( ), locationService );

        InternalTestHelper.setInternalUserNumber( 1 );
        UserService userService = new UserService( gpsUtil, rewardsService, testingService );
        User user = userService.getAllUsers( ).get( 0 );

        // WHEN
        rewardsService.calculateRewards( user );
        List<UserReward> userRewards = user.getUserRewards( );
        userService.tracker.stopTracking( );

        // THEN
        assertEquals( gpsUtil.getAttractions( ).size( ), userRewards.size( ) );
    }
}



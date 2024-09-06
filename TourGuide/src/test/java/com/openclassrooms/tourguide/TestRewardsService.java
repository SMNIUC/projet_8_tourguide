package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.openclassrooms.tourguide.service.LocationService;
import com.openclassrooms.tourguide.service.test.TestingService;
import com.openclassrooms.tourguide.service.UserService;
import org.junit.jupiter.api.BeforeAll;
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
    private static GpsUtil gpsUtil;
    private static LocationService locationService;
    private static UserService userService;
    private static RewardsService rewardsService;

    @BeforeAll
    static void setup( )
    {
        gpsUtil = new GpsUtil( );
        rewardsService = new RewardsService( gpsUtil, new RewardCentral( ) );
        TestingService testingService = new TestingService( );
        locationService = new LocationService( gpsUtil, rewardsService );
        userService = new UserService( testingService, locationService );
    }

    @Test
    public void userGetRewards( )
    {
        // GIVEN
        InternalTestHelper.setInternalUserNumber( 0 );
        User user = new User( UUID.randomUUID( ), "jon", "000", "jon@tourGuide.com" );

        // WHEN
        Attraction attraction = gpsUtil.getAttractions( ).get( 0 );
        user.addToVisitedLocations( new VisitedLocation( user.getUserId( ), attraction, new Date( ) ) );
        locationService.trackUserLocation( user );
        List<UserReward> userRewards = user.getUserRewards( );
        userService.tracker.stopTracking( );

        // THEN
        assertEquals( 1, userRewards.size( ) );
    }

    @Test
    public void isWithinAttractionProximity( )
    {
        // WHEN
        Attraction attraction = gpsUtil.getAttractions( ).get( 0 );

        // THEN
        assertTrue( rewardsService.isWithinAttractionProximity( attraction, attraction ) );
    }

//    @Disabled // Needs fixed - can throw ConcurrentModificationException
    @Test
    public void nearAllAttractions( )
    {
        //GIVEN
        rewardsService.setProximityBuffer( Integer.MAX_VALUE );
        InternalTestHelper.setInternalUserNumber( 1 );
        User user = userService.getAllUsers( ).get( 0 );

        // WHEN
        rewardsService.calculateRewards( user, user.getLastVisitedLocation( ) );
        List<UserReward> userRewards = user.getUserRewards( );
        userService.tracker.stopTracking( );

        // THEN
        assertEquals( gpsUtil.getAttractions( ).size( ), userRewards.size( ) );
    }
}

package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import com.openclassrooms.tourguide.domain.dto.ClosestAttractionsDTO;
import com.openclassrooms.tourguide.service.LocationService;
import com.openclassrooms.tourguide.service.test.TestingService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.testUtils.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.UserService;
import com.openclassrooms.tourguide.domain.User;
import tripPricer.Provider;

public class TestUserService
{
    @Test
    public void getUserLocation( )
    {
        GpsUtil gpsUtil = new GpsUtil( );
        LocationService locationService = new LocationService( new RewardCentral( ), gpsUtil );
        RewardsService rewardsService = new RewardsService( gpsUtil, new RewardCentral( ), locationService );
        InternalTestHelper.setInternalUserNumber( 0 );
        TestingService testingService = new TestingService( );
        UserService userService = new UserService( gpsUtil, rewardsService, testingService );

        User user = new User( UUID.randomUUID( ), "jon", "000", "jon@tourGuide.com" );
        VisitedLocation visitedLocation = userService.trackUserLocation( user );
        userService.tracker.stopTracking( );
        assertEquals( visitedLocation.userId, user.getUserId( ) );
    }

    @Test
    public void addUser( )
    {
        GpsUtil gpsUtil = new GpsUtil( );
        LocationService locationService = new LocationService( new RewardCentral( ), gpsUtil );
        RewardsService rewardsService = new RewardsService( gpsUtil, new RewardCentral( ), locationService );
        InternalTestHelper.setInternalUserNumber( 0 );
        TestingService testingService = new TestingService( );
        UserService userService = new UserService( gpsUtil, rewardsService, testingService );

        User user = new User( UUID.randomUUID( ), "jon", "000", "jon@tourGuide.com" );
        User user2 = new User( UUID.randomUUID( ), "jon2", "000", "jon2@tourGuide.com" );

        userService.addUser( user );
        userService.addUser( user2 );

        User retrivedUser = userService.getUser( user.getUserName( ) );
        User retrivedUser2 = userService.getUser( user2.getUserName( ) );

        userService.tracker.stopTracking( );

        assertEquals( user, retrivedUser );
        assertEquals( user2, retrivedUser2 );
    }

    @Test
    public void getAllUsers( )
    {
        GpsUtil gpsUtil = new GpsUtil( );
        LocationService locationService = new LocationService( new RewardCentral( ), gpsUtil );
        RewardsService rewardsService = new RewardsService( gpsUtil, new RewardCentral( ), locationService );
        InternalTestHelper.setInternalUserNumber( 0 );
        TestingService testingService = new TestingService( );
        UserService userService = new UserService( gpsUtil, rewardsService, testingService );

        User user = new User( UUID.randomUUID( ), "jon", "000", "jon@tourGuide.com" );
        User user2 = new User( UUID.randomUUID( ), "jon2", "000", "jon2@tourGuide.com" );

        userService.addUser( user );
        userService.addUser( user2 );

        List<User> allUsers = userService.getAllUsers( );

        userService.tracker.stopTracking( );

        assertTrue( allUsers.contains( user ) );
        assertTrue( allUsers.contains( user2 ) );
    }

    @Test
    public void trackUser( )
    {
        GpsUtil gpsUtil = new GpsUtil( );
        LocationService locationService = new LocationService( new RewardCentral( ), gpsUtil );
        RewardsService rewardsService = new RewardsService( gpsUtil, new RewardCentral( ), locationService );
        InternalTestHelper.setInternalUserNumber( 0 );
        TestingService testingService = new TestingService( );
        UserService userService = new UserService( gpsUtil, rewardsService, testingService );

        User user = new User( UUID.randomUUID( ), "jon", "000", "jon@tourGuide.com" );
        VisitedLocation visitedLocation = userService.trackUserLocation( user );

        userService.tracker.stopTracking( );

        assertEquals( user.getUserId( ), visitedLocation.userId );
    }

    @Test
    public void getFiveClosestAttractions( )
    {
        GpsUtil gpsUtil = new GpsUtil( );
        LocationService locationService = new LocationService( new RewardCentral( ), gpsUtil );
        RewardsService rewardsService = new RewardsService( gpsUtil, new RewardCentral( ), locationService );
        InternalTestHelper.setInternalUserNumber( 0 );
        TestingService testingService = new TestingService( );
        UserService userService = new UserService( gpsUtil, rewardsService, testingService );

        User user = new User( UUID.randomUUID( ), "jon", "000", "jon@tourGuide.com" );
        VisitedLocation visitedLocation = userService.trackUserLocation( user );

        // Modified to test new code
        List<ClosestAttractionsDTO> attractions = locationService.getFiveClosestAttractions( visitedLocation );

        userService.tracker.stopTracking( );

        assertEquals( 5, attractions.size( ) );
    }

    // Hors du champ d'application
    @Disabled
    public void getTripDeals( )
    {
        GpsUtil gpsUtil = new GpsUtil( );
        LocationService locationService = new LocationService( new RewardCentral( ), gpsUtil );
        RewardsService rewardsService = new RewardsService( gpsUtil, new RewardCentral( ), locationService );
        InternalTestHelper.setInternalUserNumber( 0 );
        TestingService testingService = new TestingService( );
        UserService userService = new UserService( gpsUtil, rewardsService, testingService );

        User user = new User( UUID.randomUUID( ), "jon", "000", "jon@tourGuide.com" );

        List<Provider> providers = userService.getTripDeals( user );

        userService.tracker.stopTracking( );

        assertEquals( 10, providers.size( ) );
    }
}

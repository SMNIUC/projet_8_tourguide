package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.openclassrooms.tourguide.service.LocationService;
import com.openclassrooms.tourguide.service.test.TestingService;
import com.openclassrooms.tourguide.service.UserService;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.testUtils.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.domain.User;

public class TestPerformance
{
    /*
     * A note on performance improvements:
     *
     * The number of users generated for the high volume tests can be easily
     * adjusted via this method:
     *
     * InternalTestHelper.setInternalUserNumber(100000);
     *
     *
     * These tests can be modified to suit new solutions, just as long as the
     * performance metrics at the end of the tests remains consistent.
     *
     * These are performance metrics that we are trying to hit:
     *
     * highVolumeTrackLocation: 100,000 users within 15 minutes:
     * assertTrue(TimeUnit.MINUTES.toSeconds(15) >=
     * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     *
     * highVolumeGetRewards: 100,000 users within 20 minutes:
     * assertTrue(TimeUnit.MINUTES.toSeconds(20) >=
     * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     */
//    @Disabled
    @Test
    public void highVolumeTrackLocation( )
    {
        GpsUtil gpsUtil = new GpsUtil( );
        LocationService locationService = new LocationService( new RewardCentral( ), gpsUtil );
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral( ), locationService );
        // Users should be incremented up to 100,000, and test finishes within 15
        // minutes
        InternalTestHelper.setInternalUserNumber( 10000 );
        TestingService testingService = new TestingService( );
        UserService userService = new UserService( gpsUtil, rewardsService, testingService );

        List<User> allUsers;
        allUsers = userService.getAllUsers( );

        StopWatch stopWatch = new StopWatch( );
        stopWatch.start( );
        for ( User user : allUsers )
        {
            userService.parallelizedTrackUserLocation( user );
        }
        stopWatch.stop( );
        userService.tracker.stopTracking( );

        System.out.println( "highVolumeTrackLocation: Time Elapsed: "
                + TimeUnit.MILLISECONDS.toSeconds( stopWatch.getTime( ) ) + " seconds." );
        assertTrue( TimeUnit.MINUTES.toSeconds( 15 ) >= TimeUnit.MILLISECONDS.toSeconds( stopWatch.getTime( ) ) );
    }

//    @Disabled
    @Test
    public void highVolumeGetRewards( )
    {
        GpsUtil gpsUtil = new GpsUtil( );
        LocationService locationService = new LocationService( new RewardCentral( ), gpsUtil );
        RewardsService rewardsService = new RewardsService( gpsUtil, new RewardCentral( ), locationService );

        // Users should be incremented up to 100,000, and test finishes within 20
        // minutes
        InternalTestHelper.setInternalUserNumber( 10000 );
        StopWatch stopWatch = new StopWatch( );
        stopWatch.start( );
        TestingService testingService = new TestingService( );
        UserService userService = new UserService( gpsUtil, rewardsService, testingService );

        Attraction attraction = gpsUtil.getAttractions( ).get( 0 );
        List<User> allUsers;
        allUsers = userService.getAllUsers( );
        allUsers.forEach( u -> u.addToVisitedLocations( new VisitedLocation( u.getUserId( ), attraction, new Date( ) ) ) );
        allUsers.forEach( rewardsService::calculateRewards );

        for ( User user : allUsers )
        {
            assertFalse( user.getUserRewards( ).isEmpty( ) );
        }
        stopWatch.stop( );
        userService.tracker.stopTracking( );

        System.out.println( "highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds( stopWatch.getTime( ) )
                + " seconds." );
        assertTrue( TimeUnit.MINUTES.toSeconds( 20 ) >= TimeUnit.MILLISECONDS.toSeconds( stopWatch.getTime( ) ) );
    }
}

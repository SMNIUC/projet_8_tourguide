package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.domain.UserReward;
import com.openclassrooms.tourguide.service.test.TestingService;
import com.openclassrooms.tourguide.testUtils.Tracker;
import com.openclassrooms.tourguide.domain.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;

import gpsUtil.GpsUtil;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import tripPricer.Provider;
import tripPricer.TripPricer;

import static com.openclassrooms.tourguide.service.test.TestingService.tripPricerApiKey;

/**
 * Service class that provides functionality for tracking user locations, managing user rewards,
 * and generating trip deals using the TourGuide system. This class interacts with external services
 * such as GPS utility and Rewards service to perform its operations.
 * <p>
 * It also manages a set of internal test users for testing purposes.
 * </p>
 *
 */
@Service
public class UserService
{
    private Logger logger = LoggerFactory.getLogger( UserService.class );
    ExecutorService executorService = Executors.newCachedThreadPool( );

    // Imported lib objects
    private final TripPricer tripPricer = new TripPricer( );
    private final GpsUtil gpsUtil;
    private final RewardsService rewardsService;

    private final TestingService testingService;
    public final Tracker tracker;

    boolean testMode = true;


    /**
     * Constructs a new {@code UserService} with the provided {@code GpsUtil} and {@code RewardsService}.
     * If test mode is enabled, it initializes internal users for testing purposes.
     *
     * @param gpsUtil gpsUtil service
     * @param rewardsService rewardService service
     * @param testingService testingService service
     */
    public UserService( GpsUtil gpsUtil, RewardsService rewardsService, TestingService testingService )
    {
        this.gpsUtil = gpsUtil;
        this.rewardsService = rewardsService;
        this.testingService = testingService;
        Locale.setDefault( Locale.US );

        if ( testMode )
        {
            logger.info( "TestMode enabled" );
            logger.info( "Initializing users" );
            testingService.initializeInternalUsers( );
            logger.info( "Finished initializing users" );
        }
        tracker = new Tracker( this );
        addShutDownHook( );
    }


    /**
     * Retrieves the user by their username.
     *
     * @param userName the username of the user to retrieve
     * @return the user corresponding to the given username
     */
    public User getUser( String userName )
    {
        return testingService.internalUserMap.get( userName );
    }


    /**
     * Retrieves a list of all users managed by this service.
     *
     * @return a list of all users
     */
    public List<User> getAllUsers( )
    {
        return new ArrayList<>( testingService.internalUserMap.values( ) );
    }


    /**
     * Adds a new user to the internal user map if the user does not already exist.
     *
     * @param user the user to be added
     */
    public void addUser( User user )
    {
        if ( !testingService.internalUserMap.containsKey( user.getUserName( ) ) )
        {
            testingService.internalUserMap.put( user.getUserName( ), user );
        }
    }


    /**
     * Retrieves the current location of the specified user.
     * If the user has no recorded locations, it tracks the user's location.
     *
     * @param user the user whose location is to be retrieved
     * @return the user's current or last visited location
     */
    public VisitedLocation getUserLocation( User user )
    {
        return ( !user.getVisitedLocations( ).isEmpty( ) ) ? user.getLastVisitedLocation( ) : trackUserLocation( user );
    }


    /**
     * Tracks the user's location using the GPS utility service, updates the user's visited locations,
     * and calculates rewards based on the new location.
     *
     * @param user the user whose location is to be tracked
     * @return the user's tracked location
     */
    public VisitedLocation trackUserLocation( User user )
    {
        VisitedLocation visitedLocation = gpsUtil.getUserLocation( user.getUserId( ) );
        user.addToVisitedLocations( visitedLocation );
        rewardsService.calculateRewards( user );

        return visitedLocation;
    }


    public void parallelizedTrackUserLocation( User user )
    {
        CompletableFuture.supplyAsync( () -> gpsUtil.getUserLocation( user.getUserId( ) ), executorService )
                .thenAccept( location -> {
                user.addToVisitedLocations( location );
                rewardsService.calculateRewards( user );
            } );
    }


    /**
     * Retrieves a list of trip deals for the specified user based on their reward points
     * and user preferences.
     *
     * @param user the user for whom the trip deals are to be retrieved
     * @return a list of trip deals available to the user
     */
    public List<Provider> getTripDeals( User user )
    {
        int cumulatativeRewardPoints = user.getUserRewards( ).stream( ).mapToInt( UserReward::getRewardPoints ).sum( );

        List<Provider> providers = tripPricer.getPrice( tripPricerApiKey, user.getUserId( ),
                user.getUserPreferences( ).getNumberOfAdults( ), user.getUserPreferences( ).getNumberOfChildren( ),
                user.getUserPreferences( ).getTripDuration( ), cumulatativeRewardPoints );

        user.setTripDeals( providers );

        return providers;
    }


    /**
     * Adds a shutdown hook to ensure the tracker stops tracking when the JVM shuts down.
     */
    private void addShutDownHook( )
    {
        Runtime.getRuntime( ).addShutdownHook( new Thread( tracker::stopTracking ) );
    }
}

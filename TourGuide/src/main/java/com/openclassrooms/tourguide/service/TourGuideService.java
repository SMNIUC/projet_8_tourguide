package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.tracker.Tracker;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;

import tripPricer.Provider;
import tripPricer.TripPricer;

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
public class TourGuideService
{
    private Logger logger = LoggerFactory.getLogger( TourGuideService.class );

    private final GpsUtil gpsUtil;
    private final RewardsService rewardsService;
    private final TripPricer tripPricer = new TripPricer( );
    public final Tracker tracker;

    boolean testMode = true;

    /**
     * Constructs a new {@code TourGuideService} with the provided {@code GpsUtil} and {@code RewardsService}.
     * If test mode is enabled, it initializes internal users for testing purposes.
     *
     * @param gpsUtil the GPS utility service used to track user locations
     * @param rewardsService the rewards service used to calculate and manage user rewards
     */
    public TourGuideService( GpsUtil gpsUtil, RewardsService rewardsService )
    {
        this.gpsUtil = gpsUtil;
        this.rewardsService = rewardsService;

        Locale.setDefault( Locale.US );

        if ( testMode )
        {
            logger.info( "TestMode enabled" );
            logger.info( "Initializing users" );
            initializeInternalUsers( );
            logger.info( "Finished initializing users" );
        }
        tracker = new Tracker( this );
        addShutDownHook( );
    }

    /**
     * Retrieves the list of rewards earned by the specified user.
     *
     * @param user the user whose rewards are to be retrieved
     * @return a list of rewards for the user
     */
    public List<UserReward> getUserRewards( User user )
    {
        return user.getUserRewards( );
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
        return ( !user.getVisitedLocations( ).isEmpty( ) ) ? user.getLastVisitedLocation( )
                : trackUserLocation( user );
    }

    /**
     * Retrieves the user by their username.
     *
     * @param userName the username of the user to retrieve
     * @return the user corresponding to the given username
     */
    public User getUser( String userName )
    {
        return internalUserMap.get( userName );
    }

    /**
     * Retrieves a list of all users managed by this service.
     *
     * @return a list of all users
     */
    public List<User> getAllUsers( )
    {
        return new ArrayList<>( internalUserMap.values( ) );
    }

    /**
     * Adds a new user to the internal user map if the user does not already exist.
     *
     * @param user the user to be added
     */
    public void addUser( User user )
    {
        if ( !internalUserMap.containsKey( user.getUserName( ) ) )
        {
            internalUserMap.put( user.getUserName( ), user );
        }
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

    /**
     * Retrieves a list of attractions near the specified location based on proximity.
     *
     * @param visitedLocation the location from which to search for nearby attractions
     * @return a list of attractions near the specified location
     */
    public List<Attraction> getNearByAttractions( VisitedLocation visitedLocation )
    {
        List<Attraction> nearbyAttractions = new ArrayList<>( );

        for ( Attraction attraction : gpsUtil.getAttractions( ) )
        {
            if ( rewardsService.isWithinAttractionProximity( attraction, visitedLocation.location ) )
            {
                nearbyAttractions.add( attraction );
            }
        }

        return nearbyAttractions;
    }

    /**
     * Adds a shutdown hook to ensure the tracker stops tracking when the JVM shuts down.
     */
    private void addShutDownHook( )
    {
        Runtime.getRuntime( ).addShutdownHook( new Thread( )
        {
            public void run( )
            {
                tracker.stopTracking( );
            }
        } );
    }

    /**********************************************************************************
     *
     * Methods Below: For Internal Testing
     *
     **********************************************************************************/
    private static final String tripPricerApiKey = "test-server-api-key";
    // Database connection will be used for external users, but for testing purposes
    // internal users are provided and stored in memory
    private final Map<String, User> internalUserMap = new HashMap<>( );

    /**
     * Initializes internal test users for testing purposes.
     * The number of users is determined by the InternalTestHelper.
     */
    private void initializeInternalUsers( )
    {
        IntStream.range( 0, InternalTestHelper.getInternalUserNumber( ) ).forEach( i ->
        {
            String userName = "internalUser" + i;
            String phone = "000";
            String email = userName + "@tourGuide.com";
            User user = new User( UUID.randomUUID( ), userName, phone, email );
            generateUserLocationHistory( user );

            internalUserMap.put( userName, user );
        } );
        logger.info( "Created {} internal test users.", InternalTestHelper.getInternalUserNumber( ) );
    }

    /**
     * Generates a random location history for the specified user.
     *
     * @param user the user for whom the location history is to be generated
     */
    private void generateUserLocationHistory( User user )
    {
        IntStream.range( 0, 3 ).forEach( i ->
                user.addToVisitedLocations( new VisitedLocation( user.getUserId( ),
                        new Location( generateRandomLatitude( ), generateRandomLongitude( ) ), getRandomTime( ) ) ) );
    }

    /**
     * Generates a random longitude value.
     *
     * @return a random longitude value within valid bounds
     */
    private double generateRandomLongitude( )
    {
        double leftLimit = -180;
        double rightLimit = 180;
        return leftLimit + new Random( ).nextDouble( ) * ( rightLimit - leftLimit );
    }

    /**
     * Generates a random latitude value.
     *
     * @return a random latitude value within valid bounds
     */
    private double generateRandomLatitude( )
    {
        double leftLimit = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random( ).nextDouble( ) * ( rightLimit - leftLimit );
    }

    /**
     * Generates a random date and time within the past 30 days.
     *
     * @return a random date and time
     */
    private Date getRandomTime( )
    {
        LocalDateTime localDateTime = LocalDateTime.now( ).minusDays( new Random( ).nextInt( 30 ) );
        return Date.from( localDateTime.toInstant( ZoneOffset.UTC ) );
    }
}

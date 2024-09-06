package com.openclassrooms.tourguide.service.test;

import com.openclassrooms.tourguide.domain.User;
import com.openclassrooms.tourguide.testUtils.InternalTestHelper;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class TestingService
{
    private Logger logger = LoggerFactory.getLogger( TestingService.class );

    /**********************************************************************************
     *
     * Methods Below: For Internal Testing
     *
     **********************************************************************************/
    public static final String tripPricerApiKey = "test-server-api-key";
    // Database connection will be used for external users, but for testing purposes
    // internal users are provided and stored in memory
    public final Map<String, User> internalUserMap = new HashMap<>( );

    /**
     * Initializes internal test users for testing purposes.
     * The number of users is determined by the InternalTestHelper.
     */
    public void initializeInternalUsers( )
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

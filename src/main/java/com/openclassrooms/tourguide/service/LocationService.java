package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.domain.dto.ClosestAttractionsDTO;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import lombok.Data;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;

import java.util.*;
import java.util.stream.Collectors;

@Data
@Service
public class LocationService
{
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
    private final RewardCentral getRewardCentral;

    // Proximity buffers and ranges
    private int defaultProximityBuffer 	 = 10;
    private int proximityBuffer 		 = defaultProximityBuffer;
    private int attractionProximityRange = 200;

    // External services for GPS and rewards management
    private final GpsUtil gpsUtil;


    /**
     * Resets the proximity buffer to its default value.
     *
     * <p>This method resets the proximity buffer used in distance calculations
     * back to the default proximity buffer value.</p>
     */
    public void setDefaultProximityBuffer( )
    {
        proximityBuffer = defaultProximityBuffer;
    }


    /**
     * Checks if a given location is within proximity to a specified attraction.
     *
     * <p>This method determines if a user's location is within the defined attraction proximity range.</p>
     *
     * @param attraction the {@link Attraction} to compare against
     * @param location   the {@link Location} of the user
     * @return {@code true} if the location is within the proximity range, {@code false} otherwise
     */
    public boolean isWithinAttractionProximity( Attraction attraction, Location location )
    {
        return !( getDistance( attraction, location ) > attractionProximityRange );
    }


    /**
     * Checks if a visited location is near a specified attraction using the proximity buffer.
     *
     * @param visitedLocation the {@link VisitedLocation} of the user
     * @param attraction      the {@link Attraction} to compare against
     * @return {@code true} if the visited location is within the proximity buffer, {@code false} otherwise
     */
    boolean nearAttraction( VisitedLocation visitedLocation, Attraction attraction )
    {
        // returns True if the attraction is closer than 10 miles (included) from the visited location
        return !( getDistance( attraction, visitedLocation.location ) > proximityBuffer );
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
            if ( isWithinAttractionProximity( attraction, visitedLocation.location ) )
            {
                nearbyAttractions.add( attraction );
            }
        }

        return nearbyAttractions;
    }


    /**
     * Get the closest five tourist attractions to the user - no matter how far away they are.
     *
     * @param visitedLocation the location from which to search for nearby attractions
     * @return a list of attractions near the specified location
     */
    public List<ClosestAttractionsDTO> getFiveClosestAttractions( VisitedLocation visitedLocation )
    {
        return gpsUtil.getAttractions( ).stream( )
                .map( attraction -> Map.entry( attraction, getDistance( attraction, visitedLocation.location ) ) )
                .sorted( Map.Entry.comparingByValue( ) )
                .limit( 5 )
                .map( entry -> createClosestAttractionsDTO( entry.getKey( ), entry.getValue( ), visitedLocation ) )
                .collect( Collectors.toList( ) );
    }

    /**
     * Creates a {@link ClosestAttractionsDTO} from an attraction, its distance, and the user's visited location.
     *
     * @param attraction the attraction
     * @param distance the distance from the user to the attraction
     * @param visitedLocation the user's visited location
     * @return a populated {@link ClosestAttractionsDTO}
     */
    private ClosestAttractionsDTO createClosestAttractionsDTO( Attraction attraction, Double distance, VisitedLocation visitedLocation )
    {
        ClosestAttractionsDTO attractionDTO = new ClosestAttractionsDTO( );
        attractionDTO.setAttractionLocation( attraction );
        attractionDTO.setAttractionName( attraction.attractionName );
        attractionDTO.setUserLocation( visitedLocation.location );
        attractionDTO.setUserDistanceInMilesToAttraction( distance );
        attractionDTO.setRewardPoints( getRewardCentral.getAttractionRewardPoints( attraction.attractionId, visitedLocation.userId ) );
        return attractionDTO;
    }


    /**
     * Calculates the distance in statute miles between two locations.
     *
     * <p>This method uses the Haversine formula to calculate the distance between two geographic
     * locations, taking into account the curvature of the Earth.</p>
     *
     * @param loc1 the first {@link Location}
     * @param loc2 the second {@link Location}
     * @return the distance between the two locations in statute miles
     */
    public double getDistance( Location loc1, Location loc2 )
    {
        double lat1 = Math.toRadians( loc1.latitude );
        double lon1 = Math.toRadians( loc1.longitude );
        double lat2 = Math.toRadians( loc2.latitude );
        double lon2 = Math.toRadians( loc2.longitude );

        double angle = Math.acos( Math.sin( lat1 ) * Math.sin( lat2 )
                + Math.cos( lat1 ) * Math.cos( lat2 ) * Math.cos( lon1 - lon2 ) );

        double nauticalMiles = 60 * Math.toDegrees( angle );
        return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
    }
}

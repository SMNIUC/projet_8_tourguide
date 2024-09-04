package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.domain.User;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService
{
    // Imported lib objects
    private final GpsUtil gpsUtil;
    private final RewardsService rewardsService;

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
        rewardsService.calculateRewards( user, visitedLocation );

        return visitedLocation;
    }
}

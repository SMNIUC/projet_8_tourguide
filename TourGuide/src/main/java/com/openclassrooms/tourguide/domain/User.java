package com.openclassrooms.tourguide.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.openclassrooms.tourguide.testUtils.UserPreferences;
import gpsUtil.location.VisitedLocation;
import lombok.Data;
import tripPricer.Provider;

@Data
public class User
{
    private final UUID            userId;
    private final String          userName;
    private String                phoneNumber;
    private String                emailAddress;
    private Date                  latestLocationTimestamp;
    private List<VisitedLocation> visitedLocations = new ArrayList<>( );
    private List<UserReward>      userRewards = new ArrayList<>( );
    private UserPreferences       userPreferences = new UserPreferences( );
    private List<Provider>        tripDeals = new ArrayList<>( );


    /**********************************************************************************
     *
     * Method Below: Constructor
     *
     **********************************************************************************/
    public User( UUID userId, String userName, String phoneNumber, String emailAddress )
    {
        this.userId = userId;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
    }


    /**********************************************************************************
     *
     * Methods Below: For Location
     *
     **********************************************************************************/
    public VisitedLocation getLastVisitedLocation( )
    {
        return visitedLocations.get( visitedLocations.size( ) - 1 );
    }

    public void addToVisitedLocations( VisitedLocation visitedLocation )
    {
        visitedLocations.add( visitedLocation );
    }

    public void clearVisitedLocations( )
    {
        visitedLocations.clear( );
    }

    /**********************************************************************************
     *
     * Methods Below: For Rewards
     *
     **********************************************************************************/
    public void addUserReward( UserReward userReward )
    {
        if ( userRewards.stream( ).noneMatch( r -> true ) )
        {
            userRewards.add( userReward );
        }
    }
}

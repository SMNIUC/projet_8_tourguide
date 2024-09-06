package com.openclassrooms.tourguide.controller;

import java.util.List;

import com.openclassrooms.tourguide.service.LocationService;
import com.openclassrooms.tourguide.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;

import com.openclassrooms.tourguide.domain.User;
import com.openclassrooms.tourguide.domain.UserReward;

import tripPricer.Provider;

@Controller
@RequiredArgsConstructor
public class TourGuideController
{
    private final UserService userService;
    private final LocationService locationService;

    @RequestMapping("/")
    public String index( )
    {
        return "Greetings from TourGuide!";
    }

    @RequestMapping("/getLocation")
    public VisitedLocation getLocation( @RequestParam String userName )
    {
        return userService.getUserLocation( getUser( userName ) );
    }

    //  TODO: Change this method to no longer return a List of Attractions.
    //  Instead: Get the closest five tourist attractions to the user - no matter how far away they are.
    //  Return a new JSON object that contains:
    // Name of Tourist attraction,
    // Tourist attractions lat/long,
    // The user's location lat/long,
    // The distance in miles between the user's location and each of the attractions.
    // The reward points for visiting each Attraction.
    //    Note: Attraction reward points can be gathered from RewardsCentral
    @RequestMapping("/getNearbyAttractions")
    public List<Attraction> getNearbyAttractions( @RequestParam String userName )
    {
        VisitedLocation visitedLocation = userService.getUserLocation( getUser( userName ) );
        return locationService.getNearByAttractions( visitedLocation );
    }

    @RequestMapping("/getRewards")
    public List<UserReward> getRewards( @RequestParam String userName )
    {
        User user = getUser( userName );
        return user.getUserRewards( );
    }

    @RequestMapping("/getTripDeals")
    public List<Provider> getTripDeals( @RequestParam String userName )
    {
        return userService.getTripDeals( getUser( userName ) );
    }

    private User getUser( String userName )
    {
        return userService.getUser( userName );
    }
}
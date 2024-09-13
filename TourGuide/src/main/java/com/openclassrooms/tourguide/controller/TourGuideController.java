package com.openclassrooms.tourguide.controller;

import java.util.List;

import com.openclassrooms.tourguide.domain.dto.ClosestAttractionsDTO;
import com.openclassrooms.tourguide.service.LocationService;
import com.openclassrooms.tourguide.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @RequestMapping("/getNearbyAttractions")
    public List<ClosestAttractionsDTO> getNearbyAttractions( @RequestParam String userName )
    {
        VisitedLocation visitedLocation = userService.getUserLocation( getUser( userName ) );
        return locationService.getFiveClosestAttractions( visitedLocation );
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
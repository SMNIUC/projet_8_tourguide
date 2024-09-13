package com.openclassrooms.tourguide.testUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openclassrooms.tourguide.service.UserService;
import com.openclassrooms.tourguide.domain.User;

public class Tracker extends Thread
{
    private Logger logger 							  = LoggerFactory.getLogger( Tracker.class );

    private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds( 5 );
    private final ExecutorService executorService 	  = Executors.newSingleThreadExecutor( );
    private final UserService userService;

    private boolean stop 							  = false;


    public Tracker( UserService userService )
    {
        this.userService = userService;

        executorService.submit( this );
    }

    /**
     * Assures to shut down the Tracker thread
     */
    public void stopTracking( )
    {
        stop = true;
        executorService.shutdownNow( );
    }

    @Override
    public void run( )
    {
        StopWatch stopWatch = new StopWatch( );
        while ( true )
        {
            if ( Thread.currentThread( ).isInterrupted( ) || stop )
            {
                logger.info( "Tracker stopping" );
                break;
            }

            List<User> users = userService.getAllUsers( );
            logger.info( "Begin Tracker. Tracking {} users.", users.size( ) );
            stopWatch.start( );
            users.forEach( userService::trackUserLocation );
            stopWatch.stop( );
            logger.info( "Tracker Time Elapsed: {} seconds.", TimeUnit.MILLISECONDS.toSeconds( stopWatch.getTime( ) ) );
            stopWatch.reset( );
            try
            {
                logger.info( "Tracker sleeping" );
                TimeUnit.SECONDS.sleep( trackingPollingInterval );
            } catch ( InterruptedException e )
            {
                break;
            }
        }
    }
}

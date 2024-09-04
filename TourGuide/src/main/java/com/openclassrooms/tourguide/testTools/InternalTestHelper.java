package com.openclassrooms.tourguide.testTools;

import lombok.Data;
import lombok.Getter;

@Data
public class InternalTestHelper
{
    // Set this default up to 100,000 for testing
    @Getter
    private static int internalUserNumber = 100;

    public static void setInternalUserNumber( int internalUserNumber )
    {
        InternalTestHelper.internalUserNumber = internalUserNumber;
    }
}

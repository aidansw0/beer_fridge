package io.dweet;

import java.io.IOException;
import java.text.ParseException;

/**
 * Demonstration on using Java client for dweet.io.
 * 
 * @author Khaled Bakhit <kb@khaled-bakhit.com>
 */
public class demo 
{
    
    public static void main(String[] args) throws IOException, ParseException
    {
        String thingName= "richardscoffeepot";
        
        /**
         * Get latest Dweets. 
         */
        Dweet dweet= DweetIO.getLatestDweet(thingName);
        System.out.println(dweet.getThingName()+ " said : "+ dweet.getContent().get("weight").getAsDouble() +" at "+ dweet.getCreationDate());
    }
}

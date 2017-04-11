
package io.dweet;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Dweet represents a dweet from a thing.
 * 
 * @author Khaled Bakhit <kb@khaled-bakhit.com>
 */
public class Dweet 
{ 
    private static final DateFormat date_format= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    /**
     * The json form of this dweet.
     */
    private final JsonElement jsonDweet;
    /**
     * The name of the thing this dweet belongs to.
     */
    private final String thingName;
    /**
     * The creation date of this dweet.
     */
    private final Date createdDate;
    /**
     * The content of this dweet.
     */
    private final JsonObject content;
    
    /**
     * Dweet constructor.
     * @param jsonDweet The json form of the dweet.
     * @throws java.text.ParseException Unable to parse creation date.
     */
    protected Dweet(JsonElement jsonDweet) throws ParseException
    {
        this.jsonDweet= jsonDweet;
        JsonObject j= jsonDweet.getAsJsonObject();
        thingName= j.get("thing").getAsString();
        content= j.get("content").getAsJsonObject();
        String date_string= j.get("created").getAsString(); 
        createdDate= date_format.parse(date_string);      
    }
    /**
     * Get the name of the thing this dweet belongs to.
     * @return The name of the thing this dweet belongs to. 
     */
    public String getThingName()
    {
        return thingName;
    }
    /**
     * Get the creation date of this dweet. 
     * @return The creation date of this dweet. 
     */
    public Date getCreationDate()
    {
        return createdDate;
    }
    
    /**
     * Get the content of this tweet.
     * @return Content of this tweet.
     */
    public JsonObject getContent()
    {
        return content;
    }

    @Override
    public String toString()
    {
        return jsonDweet.toString();
    }
    
}

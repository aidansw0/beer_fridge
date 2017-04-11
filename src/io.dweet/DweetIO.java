package io.dweet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * DweetIO is a wrapper class around dweet.io web API.
 * 
 * @author Khaled Bakhit <kb@khaled-bakhit.com>
 */
public class DweetIO 
{
    /**
     * Get the implemented API version.
     */
    public static final float VERSION= 1.0f;
    /**
     * DweetIO API end-point.
     */
    public static final String API_END_POINT= "dweet.io";
    /**
     * Indicate whether to use SSL encryption or not.
     * Disable for devices that do not support SSL.
     */
    public static boolean USE_SSL= true;
    /**
     * Instance of JsonParser to parse Json content .
     */
    private static final JsonParser jsonParser= new JsonParser();
    /**
     * Enable/disable usage of SSL.
     * @param enable Set to true to enable, false to disable.
     * @see #USE_SSL
     */
    public static void setSSLEnabled(boolean enable)
    {
        USE_SSL= enable;
    }
    /**
     * Check if SSL is enabled.
     * @return True if is enable, false otherwise.
     * @see #USE_SSL
     */
    public static boolean isSSLEnabled()
    {
        return USE_SSL;
    }
    /**
     * Remove a lock (no matter what it's connected to).
     * @param lock Lock to remove.
     * @param key Key that belongs to the lock.
     * @return True if successful, false otherwise.
     * @throws IOException Unable to complete the API call.
     */
    public static boolean removeLock(String lock, String key) throws IOException
    {
        if(lock== null || key == null)
            throw new NullPointerException();
        
        lock= URLEncoder.encode(lock,"UTF-8");           
        key= URLEncoder.encode(key,"UTF-8");
        
        URL url= new URL(getAPIEndPointURL()+"/remove/lock/"+lock+"?key="+key);
        HttpURLConnection connection= (HttpURLConnection) url.openConnection();  
        connection.setRequestMethod("GET");
            
        JsonObject response= readResponse(connection.getInputStream());
           
        connection.disconnect();
        
        return response.has("this") && response.get("this").getAsString().equals("succeeded");      
    }
    /**
     * Unlock a thing.
     * @param thingName Name of the thing to unlock.
     * @param key Key used to lock the thing.
     * @return True if successful, false otherwise.
     * @throws IOException Unable to complete the API call.
     */
    public static boolean unlock(String thingName, String key) throws IOException
    {
        if(key == null || thingName == null)
            throw new NullPointerException();
        
        thingName = URLEncoder.encode(thingName, "UTF-8");
        key = URLEncoder.encode(key, "UTF-8");
        
        URL url = new URL(getAPIEndPointURL() + "/unlock/" + thingName + "?key=" + key);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        JsonObject response = readResponse(connection.getInputStream());

        connection.disconnect();

        return response.has("this") && response.get("this").getAsString().equals("succeeded");
    }

     /**
     * Create an alert for a thing.
     * @param thingName Name of thing that is dweeting.
     * @param recipients A comma separated list of Email addresses to send the alert to.
     * @param condition A condition that returns a string or a true value if a condition is met.
     * @param key Key related to the thing (Can be null if thing is unlocked).
     * @return True if successful, false otherwise.
     * @throws IOException Unable to complete the API call.
     */
    public static boolean createAlert(String thingName, String recipients, String condition, String key) throws IOException
    {
        if(thingName == null || recipients == null || condition == null)
            throw new NullPointerException();
        
        condition= condition.trim();
        if(condition.length() > 2000)
        {
            throw new IOException("condition script is too large (above 2000 characters) ");
        }
        
        thingName = URLEncoder.encode(thingName, "UTF-8");
        recipients= URLEncoder.encode(recipients, "UTF-8");
        condition= URLEncoder.encode(condition, "UTF-8");

        URL url= new URL(getAPIEndPointURL()+ "/alert/"+recipients+"/when/"+ thingName+"/"+condition+((key==null)?"":"?key="+URLEncoder.encode(key,"UTF-8")));
            
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        JsonObject response= readResponse(connection.getInputStream());
           
        connection.disconnect();
        
        return (response.has("this") && response.get("this").getAsString().equals("succeeded"));
    } 
    /**
     * Remove an alert.
     * @param thingName Name of the thing to stop getting alerts from.
     * @param key Key used to lock the thing.
     * @return True if successful, false otherwise.
     * @throws IOException Unable to complete the API call.
     */
    public static boolean removeAlert(String thingName, String key) throws IOException
    { 
        if(key == null || thingName == null)
            throw new NullPointerException();
        
        thingName = URLEncoder.encode(thingName, "UTF-8");
        key = URLEncoder.encode(key, "UTF-8");
        
        URL url = new URL(getAPIEndPointURL() + "/remove/alert/for/" + thingName + "?key=" + key);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        JsonObject response = readResponse(connection.getInputStream());

        connection.disconnect();

        return response.has("this") && response.get("this").getAsString().equals("succeeded");
    }
    /**
     * Lock a thing.
     * @param thingName Name of the thing to lock.
     * @param lock Lock used to lock the thing.
     * @param key Key used to lock the thing.
     * @return True if successful, false otherwise.
     * @throws IOException Unable to complete the API call.
     */
    public static boolean lock(String thingName, String lock, String key) throws IOException
    {
        if(key == null || lock == null || thingName == null)
            throw new NullPointerException();
        
        thingName = URLEncoder.encode(thingName, "UTF-8");
        lock= URLEncoder.encode(lock,"UTF-8");    
        key= URLEncoder.encode(key,"UTF-8");
            
        URL url= new URL(getAPIEndPointURL()+"/lock/"+thingName+"?lock="+lock+"&key="+key);
        HttpURLConnection connection= (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
            
        JsonObject response= readResponse(connection.getInputStream());
           
        connection.disconnect();
          
        return response.has("this") && response.get("this").getAsString().equals("succeeded");   
    }
    /**
     * Publish a dweet.
     * @param thingName Name of thing that is dweeting.
     * @param content Content to dweet.
     * @return True if successful, false otherwise.
     * @throws IOException Unable to complete the API call.
     */
    public static boolean publish(String thingName, JsonElement content) throws IOException
    {
        return publish(thingName, content, null);
    }
    /**
     * Publish a dweet.
     * @param thingName Name of thing that is dweeting.
     * @param content Content to dweet.
     * @param key Key related to the thing (Can be null if thing is unlocked).
     * @return True if successful, false otherwise.
     * @throws IOException Unable to complete the API call.
     */
    public static boolean publish(String thingName, JsonElement content, String key) throws IOException
    {
        if(thingName == null || content == null)
            throw new NullPointerException();
        
        thingName = URLEncoder.encode(thingName, "UTF-8");
        
        URL url= new URL(getAPIEndPointURL()+ "/dweet/for/"+ thingName+((key==null)?"":"?key="+URLEncoder.encode(key,"UTF-8")));
            
        HttpURLConnection connection= (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);
            
        PrintWriter out= new PrintWriter(connection.getOutputStream());
        out.println(content.toString());
        out.flush();
        out.close();
            
        JsonObject response= readResponse(connection.getInputStream());
           
        connection.disconnect();
        
        return (response.has("this") && response.get("this").getAsString().equals("succeeded"));
    }    
     /**
     * Listen to dweets from a thing.
     * @param thingName Name of thing to listen to.
     * @param listener Listener to send received dweets to.
     */
    public static void listen(String thingName, DweetListener listener)
    {
        listen(thingName, listener, null);
    }
    
    /**
     * Listen to dweets from a thing. Blocking method call.
     * @param thingName Name of thing to listen to.
     * @param listener Listener to send received dweets to.
     * @param key Key related to the thing (Can be null if thing is unlocked).
     */
    public static void listen(String thingName, DweetListener listener, String key)
    {
        if(thingName == null || listener == null)
            throw new NullPointerException();
        try
        {
            thingName = URLEncoder.encode(thingName, "UTF-8");
            
            URL url= new URL(getAPIEndPointURL()+ "/listen/for/dweets/from/"+ thingName+((key==null)?"":"?key="+URLEncoder.encode(key,"UTF-8")));
            HttpURLConnection connection= (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("GET");
            
            Scanner scan= new Scanner(connection.getInputStream());
            String line;
 
            while(scan.hasNextLine())
            {
                if(!listener.isStillListeningTo(thingName))
                    break;
                line= scan.nextLine().trim();

                if(line.indexOf('{') == -1 || line.lastIndexOf('}') == -1)
                    continue;
                line= line.substring(line.indexOf('{'), line.lastIndexOf('}') + 1);
                line= line.replace("\\\"", "\"").replace("\\\\", "\\");
               
                listener.dweetReceived(new Dweet(jsonParser.parse(line)));
            }
            scan.close();
            connection.disconnect();
            listener.stoppedListening(thingName);
        }
        catch(Exception e)
        {
            listener.connectionLost(thingName, e);
        }
    }
    /**
     * Get all dweets for a thing.
     * @param thingName Name of the thing to get dweets from.
     * @return List of dweets or null if not available.
     * @throws IOException Unable to complete the API call. 
     * @throws java.text.ParseException Unable to parse creation date in dweets.
     */
    public static List<Dweet> getAllDweets(String thingName) throws IOException, ParseException
    {
       return getAllDweets(thingName, null); 
    }
    /**
     * Get all dweets for a thing.
     * @param thingName Name of the thing to get dweets from.
     * @param key Key related to the thing (Can be null if thing is unlocked).
     * @return List of dweets or null if not available.
     * @throws IOException Unable to complete the API call. 
     * @throws java.text.ParseException Unable to parse creation date in dweets.
     */
    public static List<Dweet> getAllDweets(String thingName, String key) throws IOException, ParseException
    {
        if(thingName == null)
            throw new NullPointerException();
        
        thingName = URLEncoder.encode(thingName, "UTF-8");
        
        URL url= new URL(getAPIEndPointURL()+"/get/dweets/for/"+thingName+((key==null)?"":"?key="+URLEncoder.encode(key,"UTF-8")));
          
        HttpURLConnection connection= (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
            
        JsonObject response= readResponse(connection.getInputStream());
           
        connection.disconnect();
          
        if(response.has("this") && response.get("this").getAsString().equals("succeeded"))
        {
            List<Dweet> dweets= new LinkedList<Dweet>();

            JsonArray arr= response.getAsJsonArray("with");    
            Iterator<JsonElement> it= arr.iterator();
            
            while(it.hasNext())
                dweets.add(new Dweet(it.next()));

            return dweets;
        }
        return null;
    }
    /**
     * Get the latest dweet for a thing.
     * @param thingName Name of the thing to get the latest dweet from.
     * @return Latest dweet or null if not available.
     * @throws IOException Unable to complete the API call. 
     * @throws java.text.ParseException Unable to parse creation date in dweets.
     */
    public static Dweet getLatestDweet(String thingName) throws IOException, ParseException
    {
        return getLatestDweet(thingName, null);
    }
   
    /**
     * Get the latest dweet for a thing.
     * @param thingName Name of the thing to get the latest dweet from.
     * @param key Key related to the thing (Can be null if thing is unlocked).
     * @return Latest dweet or null if not available.
     * @throws IOException Unable to complete the API call. 
     * @throws java.text.ParseException Unable to parse creation date in dweets.
     */
    public static Dweet getLatestDweet(String thingName, String key) throws IOException, ParseException
    {
        if(thingName == null)
            throw new NullPointerException();
        
        thingName = URLEncoder.encode(thingName, "UTF-8");
        
        URL url= new URL(getAPIEndPointURL()+"/get/latest/dweet/for/"+thingName+((key==null)?"":"?key="+URLEncoder.encode(key,"UTF-8")));
        HttpURLConnection connection= (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
         
        JsonObject response= readResponse(connection.getInputStream());
           
        connection.disconnect();
            
        if(response.has("this") && response.get("this").getAsString().equals("succeeded"))
        {
            JsonArray arr= response.getAsJsonArray("with");
            if(arr.size() == 0)
                return null;
            return new Dweet(arr.remove(0)); 
        }
        return null;
    }
    /**
     * Get the dweet.io API end point URL.
     * @return dweet.io API end point URL.
     */
    public static String getAPIEndPointURL()
    {
        return "http"+((USE_SSL)?"s":"")+"://"+API_END_POINT;   
    }
    
    private static JsonObject readResponse(InputStream in)
    {
        Scanner scan= new Scanner(in);
        StringBuilder sn= new StringBuilder();
        while(scan.hasNext())
            sn.append(scan.nextLine()).append('\n');
        scan.close();
        return jsonParser.parse( sn.toString()).getAsJsonObject();
    }
}

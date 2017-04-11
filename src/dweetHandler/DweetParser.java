package dweetHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import gui.KegManager;

/**
 * This class is responsible for reading and parsing the JSON data polled from
 * Dweet.io.
 * 
 * @author Aidan
 *
 */
public class DweetParser extends TimerTask {

    private final KegManager kegManager;
    private final String urlString;

    public DweetParser(KegManager kegManager, String url) {
        this.kegManager = kegManager;
        this.urlString = url;
    }

    /**
     * Makes an HTTP request to the given Dweet url then reads and parses the
     * JSON data. Updated values are then sent to the KegManager via
     * sendToManager().
     */
    @Override
    public void run() {
        BufferedReader reader;
        String inputLine;

        try {
            // Read JSON string form URL
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            inputLine = reader.readLine();

            JSONObject jsonDweet = (JSONObject) new JSONTokener(inputLine).nextValue();
            JSONArray with = jsonDweet.getJSONArray("with");
            JSONObject withObject = with.getJSONObject(0); // with only contains
                                                           // one element
            JSONObject content = withObject.getJSONObject("content");

            sendToManager(ContentType.WEIGHT, content.getDouble("weight"));
            sendToManager(ContentType.TEMP, content.getDouble("temp"));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends newly polled values from the Dweet url to the KegManager.
     * 
     * @param type, ContentType which gives the type of value to be sent.
     * @param val, double value polled from the Dweet url.
     */
    private void sendToManager(ContentType type, double val) {
        kegManager.updateContent(type, val);
    }

}

package backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;

/**
 * This class manages updates to the Keg stats by polling dweet data at
 * scheduled time intervals.
 * 
 * @author Aidan
 *
 */
public class KegManager {

    private final String DWEET_URL = "https://dweet.io/get/latest/dweet/for/teradici-beer-fridge";
    private final long DWEET_REFRESH_RATE = 5000; // time in ms

    private final ReadOnlyIntegerWrapper weight;
    private final ReadOnlyIntegerWrapper temp;

    public KegManager() {
        DweetParser dweetParser = new DweetParser(this, DWEET_URL);
        Timer timer = new Timer();
        timer.schedule(dweetParser, 0, DWEET_REFRESH_RATE);

        weight = new ReadOnlyIntegerWrapper();
        temp = new ReadOnlyIntegerWrapper();
    }

    /**
     * @return ReadOnlyIntegerProperty of weight.
     */
    public ReadOnlyIntegerProperty weightProperty() {
        return weight.getReadOnlyProperty();
    }

    /**
     * @return ReadOnlyIntegerProperty of temp.
     */
    public ReadOnlyIntegerProperty tempProperty() {
        return temp.getReadOnlyProperty();
    }

    /**
     * Modifies the value of weight and temp fields with new data polled
     * from Dweet by a DweetParser object.
     * 
     * @param type,
     *            ContentType which indicates the type of value to be updated
     *            (weight/temp).
     * @param val,
     *            double value polled from Dweet.
     */
    public void updateContent(ContentType type, double val) {
        if (type == ContentType.WEIGHT) {
            weight.set(convertWeightToHeight(val));
        } else if (type == ContentType.TEMP) {
            temp.set(convertTempToHeight(val));
        }
    }

    /**
     * Converts the value polled from Dweet into an int value which represents the height of the respective GUI element.
     * 
     * @param weight, double value polled from Dweet.
     * @return an int which gives the height.
     */
    private int convertWeightToHeight(double weight) {
//        double percentage = weight / 1750 * 100;
//
//        if (percentage > 100) {
//            percentage = 100;
//        } else if (percentage < 0) {
//            percentage = 0;
//        }

        // Range in pixels is between empty and full pot is 150px and 460px out
        // of 600px
        // Translates to between 25% to 77%

        return (int) weight;
    }

    /**
     * Converts the value polled from Dweet into an int value which represents the height of the respective GUI element.
     * 
     * @param temp, double value polled from Dweet.
     * @return an int which gives the height.
     */
    private int convertTempToHeight(double temp) {
//        double percentage = temp / 83.5 * 100;

//        if (percentage > 100) {
//            percentage = 100;
//        } else if (percentage < 0) {
//            percentage = 0;
//        }

        // Range in pixels is between empty and full pot is 150px and 460px out
        // of 600px
        // Translates to between 25% to 77%

        return (int) temp;
    }

}





/**
 * This class is responsible for reading and parsing the JSON data polled from
 * Dweet.io.
 * 
 * @author Aidan
 *
 */
class DweetParser extends TimerTask {

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
            //e.printStackTrace();
        } catch (JSONException e) {
            //e.printStackTrace();
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


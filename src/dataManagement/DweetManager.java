package dataManagement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * This class manages updates to the Keg stats by polling dweet data at
 * scheduled time intervals.
 * 
 * @author Aidan
 *
 */
public class DweetManager {

    private static final String DWEET_URL = "https://dweet.io/get/latest/dweet/for/beer-test";
    private static final long DWEET_REFRESH_RATE = 5000; // time in ms

    private final ReadOnlyDoubleWrapper weight;
    private final ReadOnlyDoubleWrapper temp;

    public DweetManager() {
        DweetParser dweetParser = new DweetParser(this, DWEET_URL);
        Timer timer = new Timer();
        timer.schedule(dweetParser, 0, DWEET_REFRESH_RATE);

        weight = new ReadOnlyDoubleWrapper();
        temp = new ReadOnlyDoubleWrapper();
    }

    /**
     * @return ReadOnlyIntegerProperty of weight.
     */
    public ReadOnlyDoubleProperty weightProperty() {
        return weight.getReadOnlyProperty();
    }

    /**
     * @return ReadOnlyIntegerProperty of temp.
     */
    public ReadOnlyDoubleProperty tempProperty() {
        return temp.getReadOnlyProperty();
    }

    /**
     * Modifies the value of weight and temp fields with new data polled from
     * Dweet by a DweetParser object.
     * 
     * @param type
     *            ContentType which indicates the type of value to be updated
     *            (weight/temp).
     * @param val
     *            double value polled from Dweet.
     */
    public void updateContent(ContentType type, double val) {
        if (type == ContentType.WEIGHT) {
            weight.set(val);
        } else if (type == ContentType.TEMP) {
            temp.set(val);
        }
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

    private final DweetManager kegManager;
    private final String urlString;

    public DweetParser(DweetManager kegManager, String url) {
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
     * @param type
     *            ContentType which gives the type of value to be sent.
     * @param val
     *            double value polled from the Dweet url.
     */
    private void sendToManager(ContentType type, double val) {
        kegManager.updateContent(type, val);
    }

}

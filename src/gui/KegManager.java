package gui;

import java.util.Timer;

import dweetHandler.ContentType;
import dweetHandler.DweetParser;
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
            System.out.println(weight.get());
        } else if (type == ContentType.TEMP) {
            temp.set(convertTempToHeight(val));
            System.out.println(temp.get());
        }
    }

    /**
     * Converts the value polled from Dweet into an int value which represents the height of the respective GUI element.
     * 
     * @param weight, double value polled from Dweet.
     * @return an int which gives the height.
     */
    private int convertWeightToHeight(double weight) {
        double percentage = weight / 1750 * 100;

        if (percentage > 100) {
            percentage = 100;
        } else if (percentage < 0) {
            percentage = 0;
        }

        // Range in pixels is between empty and full pot is 150px and 460px out
        // of 600px
        // TrarectangleHeightnslates to between 25% to 77%

        return (int) (percentage * 0.38 + 20);
    }

    /**
     * Converts the value polled from Dweet into an int value which represents the height of the respective GUI element.
     * 
     * @param weight, double value polled from Dweet.
     * @return an int which gives the height.
     */
    private int convertTempToHeight(double temp) {
        double percentage = temp / 83.5 * 100;

        if (percentage > 100) {
            percentage = 100;
        } else if (percentage < 0) {
            percentage = 0;
        }

        // Range in pixels is between empty and full pot is 150px and 460px out
        // of 600px
        // TrarectangleHeightnslates to between 25% to 77%

        return (int) (percentage * 0.52 + 25);
    }

}

package gui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import dataManagement.DweetManager;

import static java.lang.Math.round;

/**
 * This class refreshes the data labels and charts in the GUI.
 *
 * @author Richard
 */
public class KegManager {
    private static final int MIN_DATA_POINTS        = 10;
    private static final int MAX_DATA_POINTS        = 2000;
    private static final int Y_MIN                  = 270;
    private static final int Y_MAX                  = 285;
    private static final double METER_HEIGHT        = 232.0;
    private static final int CHART_REFRESH_RATE     = 2100; // time in ms

    private final Label tempLabel = new Label("273\u00B0K");
    private final Label weightLabel = new Label("30L");
    private final Label adjustTare = new Label("30L");
    private final Polygon weightMeter = new Polygon();
    private final XYChart.Series<Number, Number> tempData = new XYChart.Series<>();
    private final NumberAxis xAxis = new NumberAxis(0, MIN_DATA_POINTS + 1, 1);
    private final DweetManager beerKeg;

    private double taredValue;
    private int maxKegWeight = 30;
    private double sequence = 0;
    private boolean displayKelvin = false;

    public KegManager(DweetManager dweetManager) {
        beerKeg = dweetManager;

        Timeline animation;
        animation = new Timeline();
        animation.getKeyFrames().add(new KeyFrame(Duration.millis(CHART_REFRESH_RATE),
                (ActionEvent actionEvent) -> updateData()));
        animation.setCycleCount(Animation.INDEFINITE);
        animation.play();
    }

    /**
     * Get the current tare value
     *
     * @return taredValue
     */
    public double getTare() {
        return taredValue;
    }

    /**
     * Set the current tare value
     *
     * @param tare, double tare
     */
    public void setTare(double tare) {
        taredValue = tare;
    }

    /**
     * Manually adjusts the tare value up or down by 1 litre
     * when called. This method will also update the text displays
     * and the weight meter drawn in the GUI.
     *
     * @param up
     *          true observed weight should be increased, which
     *          actually decreases the current tare value set, and
     *          false if the observed weight should decrease.
     */
    public void adjustTareValue(boolean up) {
        int currentWeight = beerKeg.weightProperty().intValue();
        List <Double> updatedCoord;

        if (up) {
            if (returnTaredWeight(currentWeight) < maxKegWeight) {
                taredValue--;
            }
        }
        else {
            if (returnTaredWeight(currentWeight) > 0) {
                taredValue++;
            }
        }
        adjustTare.setText(returnTaredWeight(currentWeight) + "L");
        updatedCoord = calculateCoordinates(returnTaredWeight(currentWeight));
        for (int i=0; i<4; i++) {
            weightMeter.getPoints().set(i,updatedCoord.get(i));
        }
    }

    /**
     * Set the max weight of the keg used for calculating the percentage
     * for the weight meter to be drawn.
     *
     * @param maxKegWeight, int maxKegWeight
     */
    public void setMaxKegWeight(int maxKegWeight) { this.maxKegWeight = maxKegWeight; }

    /**
     * This is used in the case a new keg is tapped. This will get the current weight from
     * the sensor and adjust the tare value such that the observed weight is the max weight
     */
    public void tareToMaxWeight() {
        List <Double> updatedCoord;
        int weightFromSensor = beerKeg.weightProperty().intValue();
        taredValue = beerKeg.weightProperty().intValue();

        // Update GUI
        weightLabel.setText(returnTaredWeight(weightFromSensor) + "L");
        adjustTare.setText(returnTaredWeight(weightFromSensor) + "L");
        updatedCoord = calculateCoordinates(returnTaredWeight(weightFromSensor));
        for (int i=0; i<4; i++) {
            weightMeter.getPoints().set(i,updatedCoord.get(i));
        }
    }

    /**
     * Creates line chart for visualizing temperature
     *
     * @return chart object
     */
    public Parent createLineChart() {
        LineChart<Number, Number> chart;
        final NumberAxis yAxis = new NumberAxis(Y_MIN - 1, Y_MAX + 1, 0.1);

        // setup chart
        chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        xAxis.setForceZeroInRange(false);

        // create some starting data
        tempData.getData().add(new XYChart.Data<Number, Number>(++sequence, 280));
        chart.getData().add(tempData);

        return chart;
    }

    /**
     * Create label used to display temperature
     *
     * @return Label for temperature
     */
    public Label createTempLabel() {
        tempLabel.onMouseClickedProperty().setValue(event -> {
            displayKelvin = !displayKelvin;
            tempLabel.setText("...");
        } );
        return tempLabel;
    }

    /**
     * Create label used to display weight/volume
     *
     * @return Label for weight
     */
    public Label createWeightLabel() {
        return weightLabel;
    }

    /**
     * Create label used to display weight/volume for taring
     *
     * @return Label for weight
     */
    public Label createTareLabel() {
        adjustTare.setText(returnTaredWeight(beerKeg.weightProperty().doubleValue()) + "L");
        adjustTare.getStyleClass().add("beer-display");
        adjustTare.setTextFill(Paint.valueOf("aaaaaa"));
        return adjustTare;
    }

    /**
     * Creates polygon used to visualize weight/volume
     *
     * @return Polygon used to visualize weight
     */
    public Polygon createWeightMeter() {
        weightMeter.getPoints().addAll(4.7, 0.0,
                                        71.8, 0.0,
                                        30.6, METER_HEIGHT,
                                        0.0, METER_HEIGHT);

        return weightMeter;
    }

    /**
     * Calculates new polygon coordinates used for redrawing
     *
     * @param weight, weight used to calculate percentage of beer left
     * @return List with updated coordinates
     */
    private List<Double> calculateCoordinates (double weight) {
        List <Double> coords = new ArrayList<Double>();
        double newY = METER_HEIGHT - METER_HEIGHT * weight / maxKegWeight;
        double newX1, newX2;

        newX1 = 4.7 * weight / maxKegWeight;
        newX2 = (71.8 - 30.6) * weight / maxKegWeight + 30.6;

        coords.add(newX1);
        coords.add(newY);
        coords.add(newX2);
        coords.add(newY);

        return coords;
    }

    /**
     * Returns the tared weight calculated from tareValue. This method
     * will also limit the observed weight between 0 and maxWeight.
     *
     * @param weightFromSensor, weight obtained from the sensor
     * @return value adjusted for the tare offset
     */
    private int returnTaredWeight(double weightFromSensor) {
        double retVal = weightFromSensor - taredValue + maxKegWeight;

        if (retVal > maxKegWeight) {
            retVal = maxKegWeight;
        } else if (retVal < 0) {
            retVal = 0;
        }

        // round to nearest int for now
        retVal = round(retVal);

        return (int) retVal;
    }

    /**
     * Method used by timeline to periodically update GUI
     */
    private void updateData() {
        List <Double> updatedCoord;
        double weightFromSensor = beerKeg.weightProperty().doubleValue();

        tempData.getData().add(new XYChart.Data<Number, Number>(++sequence, beerKeg.tempProperty().doubleValue()));
        weightLabel.setText(returnTaredWeight(weightFromSensor) + "L");
        adjustTare.setText(returnTaredWeight(weightFromSensor) + "L");

        // Switch between Kelvin and Celsius
        if (displayKelvin) {
            tempLabel.setText(beerKeg.tempProperty().intValue() + "\u00B0K");
        }
        else {
            int celsius = beerKeg.tempProperty().intValue() - 273;
            tempLabel.setText(celsius + "\u00B0C");
        }

        // Update coordinates for weight polygon
        updatedCoord = calculateCoordinates(returnTaredWeight(weightFromSensor));
        for (int i=0; i<4; i++) {
            weightMeter.getPoints().set(i,updatedCoord.get(i));
        }

        // delete old data
        if (sequence > MAX_DATA_POINTS) {
            tempData.getData().remove(0);
        }

        // move x axis
        if (sequence > MAX_DATA_POINTS - 1) {
            xAxis.setLowerBound(xAxis.getLowerBound() + 1);
            xAxis.setUpperBound(xAxis.getUpperBound() + 1);
        }
        else if (sequence > MIN_DATA_POINTS - 1) {
            xAxis.setUpperBound(xAxis.getUpperBound() + 1);
        }
    }
}

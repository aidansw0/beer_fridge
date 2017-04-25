package gui;

import backend.KegManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class gets data from KegManager
 * and updates the line chart and temperature label
 *
 * @author Richard
 */

public class DataManager {
    private static final int MIN_DATA_POINTS        = 10;
    private static final int MAX_DATA_POINTS        = 2000;
    private static final int Y_MIN                  = 270;
    private static final int Y_MAX                  = 285;
    private static final double METER_HEIGHT        = 232.0;
    private static final int MAX_KEG_WEIGHT         = 30;
    private static final int CHART_REFRESH_RATE     = 2100; // time in ms

    private final Label tempLabel = new Label("273\u00B0K");
    private final Label weightLabel = new Label("30L");
    private final Polygon weightMeter = new Polygon();
    private final XYChart.Series<Number, Number> tempData = new XYChart.Series<>();
    private final NumberAxis xAxis = new NumberAxis(0, MIN_DATA_POINTS + 1, 1);
    private final KegManager beerKeg;

    private double sequence = 0;
    private boolean displayKelvin = false;

    public DataManager(KegManager kegManager) {
        beerKeg = kegManager;

        Timeline animation;
        animation = new Timeline();
        animation.getKeyFrames().add(new KeyFrame(Duration.millis(CHART_REFRESH_RATE),
                (ActionEvent actionEvent) -> updateData()));
        animation.setCycleCount(Animation.INDEFINITE);
        animation.play();
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
    private List calculateCoordinates (int weight) {
        List <Double> coords = new ArrayList<Double>();
        double newY = METER_HEIGHT - METER_HEIGHT * weight / MAX_KEG_WEIGHT;
        double newX1, newX2;

        newX1 = 4.7 * weight / MAX_KEG_WEIGHT;
        newX2 = (71.8 - 30.6) * weight / MAX_KEG_WEIGHT + 30.6;

        coords.add(newX1);
        coords.add(newY);
        coords.add(newX2);
        coords.add(newY);

        return coords;
    }

    /**
     * Method used by timeline to periodically update GUI
     */
    private void updateData() {
        List <Double> updatedCoord;

        tempData.getData().add(new XYChart.Data<Number, Number>(++sequence, beerKeg.tempProperty().doubleValue()));
        weightLabel.setText(beerKeg.weightProperty().intValue() + "L");

        // Switch between Kelvin and Celsius
        if (displayKelvin) {
            tempLabel.setText(beerKeg.tempProperty().intValue() + "\u00B0K");
        }
        else {
            int celsius = beerKeg.tempProperty().intValue() - 273;
            tempLabel.setText(celsius + "\u00B0C");
        }

        // Update coordinates for weight polygon
        updatedCoord = calculateCoordinates(beerKeg.weightProperty().intValue());
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

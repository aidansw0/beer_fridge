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

    private Label tempLabel;
    private Label weightLabel;
    private Polygon weightMeter;
    private LineChart<Number, Number> chart;
    private XYChart.Series<Number, Number> tempData;
    private NumberAxis xAxis;

    private double sequence = 0;
    private final int MAX_DATA_POINTS = 100, Y_MIN = 250, Y_MAX = 290;
    private final double METER_HEIGHT = 232.0;
    private final int MAX_KEG_WEIGHT = 30;
    private final KegManager beerKeg;

    public DataManager(KegManager kegManager) {
        final int CHART_REFRESH_RATE = 2100; // time in ms
        beerKeg = kegManager;

        Timeline animation;
        animation = new Timeline();
        animation.getKeyFrames().add(new KeyFrame(Duration.millis(CHART_REFRESH_RATE),
                (ActionEvent actionEvent) -> updateData()));
        animation.setCycleCount(Animation.INDEFINITE);
        animation.play();
    }

    public Parent createChart() {
        xAxis = new NumberAxis(0, MAX_DATA_POINTS + 1, 2);
        final NumberAxis yAxis = new NumberAxis(Y_MIN - 1, Y_MAX + 1, 1);

        // setup chart
        chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        xAxis.setForceZeroInRange(false);

        // add starting data
        tempData = new XYChart.Series<>();

        // create some starting data
        tempData.getData().add(new XYChart.Data<Number, Number>(++sequence, 273));
        chart.getData().add(tempData);

        return chart;
    }

    public Label createTempLabel() {
        tempLabel = new Label("273\u00B0K");
        tempLabel.onMouseClickedProperty().setValue(event -> System.out.println("Pressed"));
        return tempLabel;
    }

    public Label createWeightLabel() {
        weightLabel = new Label("30L");
        return weightLabel;
    }

    public Polygon createWeightMeter() {
        weightMeter = new Polygon();
        weightMeter.getPoints().addAll(4.7, 0.0,
                                        71.8, 0.0,
                                        30.6, METER_HEIGHT,
                                        0.0, METER_HEIGHT);

        return weightMeter;
    }

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

    private void updateData() {
        List <Double> updatedCoord;

        tempData.getData().add(new XYChart.Data<Number, Number>(++sequence, beerKeg.tempProperty().doubleValue()));
        tempLabel.setText(beerKeg.tempProperty().intValue() + "\u00B0K");
        weightLabel.setText(beerKeg.weightProperty().intValue() + "L");

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

        simulateNewData();
    }

    private void simulateNewData() {
        final String DWEET_URL = "https://dweet.io/dweet/for/teradici-beer-fridge";
        Random rnd = new Random();
        int weight = rnd.nextInt(30);
        int temp = 260 + rnd.nextInt(20);

        try {
            URL url = new URL(DWEET_URL + "?weight=" + weight + "&temp=" + temp);
            url.openStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

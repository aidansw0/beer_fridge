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
import javafx.util.Duration;

import java.util.Random;

/**
 * This class gets requests new data from KegManager
 * and produces a live-update line chart.
 *
 * @author Richard
 */

public class TemperatureChart {

    private LineChart<Number, Number> chart;
    private XYChart.Series<Number, Number> dataSeries;
    private NumberAxis xAxis;
    private Timeline animation;
    private double sequence = 0;
    private double y = 10;
    private final int MAX_DATA_POINTS = 50, MAX = 280, MIN = 270;

    public TemperatureChart() {
        animation = new Timeline();
        animation.getKeyFrames().add(new KeyFrame(Duration.millis(200),
                (ActionEvent actionEvent) -> plotTime()));
        animation.setCycleCount(Animation.INDEFINITE);
        animation.play();
    }

    public Parent createContent() {
        xAxis = new NumberAxis(0, MAX_DATA_POINTS + 1, 2);
        final NumberAxis yAxis = new NumberAxis(MIN - 1, MAX + 1, 1);
        chart = new LineChart<>(xAxis, yAxis);

        // setup chart
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        xAxis.setForceZeroInRange(false);

        // add starting data
        dataSeries = new XYChart.Series<>();

        // create some starting data
        dataSeries.getData().add(new XYChart.Data<Number, Number>(++sequence, y));
        chart.getData().add(dataSeries);

        return chart;
    }

    private void plotTime() {
        dataSeries.getData().add(new XYChart.Data<Number, Number>(++sequence, getNextValue()));

        // delete old data
        if (sequence > MAX_DATA_POINTS) {
            dataSeries.getData().remove(0);
        }

        // move x axis
        if (sequence > MAX_DATA_POINTS - 1) {
            xAxis.setLowerBound(xAxis.getLowerBound() + 1);
            xAxis.setUpperBound(xAxis.getUpperBound() + 1);
        }
    }

    private int getNextValue(){
        Random rand = new Random();
        return rand.nextInt((MAX - MIN) + 1) + MIN;
    }
}

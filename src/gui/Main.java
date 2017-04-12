package gui;
import backend.KegManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.Random;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }
    private LineChart<Number, Number> chart;
    private XYChart.Series<Number, Number> dataSeries;
    private NumberAxis xAxis;
    private Timeline animation;
    private double sequence = 0;
    private double y = 10;
    private final int MAX_DATA_POINTS = 50, MAX = 280, MIN = 270;

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

    @Override
    public void start(Stage primaryStage) throws Exception {
        Stage window;
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        //KegManager beerKeg = new KegManager();

        window = primaryStage;
        window.setTitle("Beer Keg Monitor");
        window.isFullScreen();
        window.initStyle(StageStyle.UNDECORATED);

        Font fontLato = Font.loadFont(getClass()
                        .getResourceAsStream("/css/Lato-Hairline.ttf"), 80);

        //Animated Temperature Chart
        animation = new Timeline();
        animation.getKeyFrames().add(new KeyFrame(Duration.millis(200),
                        (ActionEvent actionEvent) -> plotTime()));
        animation.setCycleCount(Animation.INDEFINITE);
        animation.play();

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

        //Keg Frame
        Image img1 = new Image("img/keg.png");
        ImageView keg = new ImageView();
        keg.setImage(img1);
        keg.setFitHeight(345);
        keg.setPreserveRatio(true);

        Image img2 = new Image("img/kegheader.png");
        ImageView kegheader = new ImageView();
        kegheader.setImage(img2);
        kegheader.setFitHeight(53);
        kegheader.setPreserveRatio(true);

        Polygon kegMeter = new Polygon();
        kegMeter.setFill(Color.web("06d3ce"));
        kegMeter.getPoints().addAll(4.7, 0.0,
                                    71.8, 0.0,
                                    30.6, 232.1,
                                    0.0, 232.3);

        Label kegVolume = new Label ("25L");
        kegVolume.setTextFill(Color.web("cacaca"));
        kegVolume.setFont(fontLato);

        StackPane kegMeterStack = new StackPane();
        kegMeterStack.getChildren().add(kegMeter);
        kegMeterStack.getChildren().add(kegVolume);

        StackPane.setAlignment(kegVolume, Pos.BOTTOM_LEFT);
        StackPane.setMargin(kegVolume, new Insets(0,0,25,60));
        StackPane.setAlignment(kegMeter, Pos.BOTTOM_LEFT);
        StackPane.setMargin(kegMeter, new Insets(50,0,48,5));

        BorderPane kegFrame = new BorderPane();
        kegFrame.setPrefSize(445,450);
        kegFrame.setStyle("-fx-background-color: #2d2d2d");
        kegFrame.setTop(kegheader);
        kegFrame.setLeft(keg);
        kegFrame.setCenter(kegMeterStack);

        BorderPane.setAlignment(keg, Pos.BOTTOM_LEFT);
        BorderPane.setMargin(keg, new Insets(15,10,15,30));

        // Temperature Frame
        Image img3 = new Image("img/tempheader.png");
        ImageView tempheader = new ImageView();
        tempheader.setImage(img3);
        tempheader.setFitHeight(53);
        tempheader.setPreserveRatio(true);

        Label temperature = new Label("273\u00B0K");
        temperature.setTextFill(Color.web("cacaca"));
        temperature.setFont(fontLato);

        BorderPane tempFrame = new BorderPane();
        tempFrame.setPrefSize(715,270);
        tempFrame.setMaxWidth(715);
        tempFrame.setStyle("-fx-background-color: #2d2d2d");
        tempFrame.setTop(tempheader);
        tempFrame.setCenter(chart);
        tempFrame.setRight(temperature);

        BorderPane.setAlignment(temperature, Pos.TOP_RIGHT);
        BorderPane.setMargin(temperature, new Insets(5,30,0,0));

        // Voting Frame
        Image img4 = new Image("img/votingheader.png");
        ImageView votingheader = new ImageView();
        votingheader.setImage(img4);
        votingheader.setFitHeight(53);
        votingheader.setPreserveRatio(true);

        BorderPane votingFrame = new BorderPane();
        votingFrame.setPrefSize(715,150);
        votingFrame.setMaxWidth(715);
        votingFrame.setStyle("-fx-background-color: #2d2d2d");
        votingFrame.setTop(votingheader);

        // Combine Temperature and Voting Frame
        BorderPane rightPane = new BorderPane();
        rightPane.setPrefSize(715,450);
        rightPane.setTop(tempFrame);
        rightPane.setCenter(votingFrame);

        BorderPane.setAlignment(tempFrame, Pos.TOP_LEFT);
        BorderPane.setMargin(tempFrame, new Insets(0,0,15,0));
        BorderPane.setAlignment(votingFrame, Pos.TOP_LEFT);
        BorderPane.setMargin(votingFrame, new Insets(15,0,0,0));

        // Footer Frame
        Image img5 = new Image("img/footerheader.png");
        ImageView footerheader = new ImageView();
        footerheader.setImage(img5);
        footerheader.setFitHeight(53);
        footerheader.setPreserveRatio(true);

        Image img6 = new Image("img/teralogo.png");
        ImageView teralogo = new ImageView();
        teralogo.setImage(img6);
        teralogo.setFitHeight(146);
        teralogo.setPreserveRatio(true);

        Label currentKeg = new Label("Steamworks IPA");
        currentKeg.setTextFill(Color.web("cacaca"));
        currentKeg.setFont(fontLato);

        BorderPane footerFrame = new BorderPane();
        footerFrame.setPrefSize(1190,220);
        footerFrame.setMaxWidth(1190);
        footerFrame.setStyle("-fx-background-color: #2d2d2d");
        footerFrame.setTop(footerheader);
        footerFrame.setLeft(currentKeg);
        footerFrame.setRight(teralogo);

        BorderPane.setAlignment(currentKeg, Pos.BOTTOM_LEFT);
        BorderPane.setMargin(currentKeg, new Insets(5,0,0,14));
        BorderPane.setAlignment(teralogo, Pos.BOTTOM_RIGHT);
        BorderPane.setMargin(teralogo, new Insets(5,0,15,0));

        // Full Scene
        BorderPane root = new BorderPane();
        root.setPrefSize(1190,450);
        root.setStyle("-fx-background-color: #1e1e1e");
        root.setLeft(kegFrame);
        root.setCenter(rightPane);
        root.setBottom(footerFrame);

        BorderPane.setAlignment(kegFrame, Pos.CENTER_RIGHT);
        BorderPane.setMargin(kegFrame, new Insets(45,15,15,50));
        BorderPane.setAlignment(rightPane, Pos.CENTER_LEFT);
        BorderPane.setMargin(rightPane, new Insets(45,50,15,15));
        BorderPane.setAlignment(footerFrame, Pos.TOP_LEFT);
        BorderPane.setMargin(footerFrame, new Insets(15,50,45,50));

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add("css/linechart.css");
        window.setScene(scene);
        window.show();
    }
}
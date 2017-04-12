package gui;
import backend.KegManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
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

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Stage window;
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        KegManager beerKeg = new KegManager();
        DataManager temperatureData = new DataManager(beerKeg);

        window = primaryStage;
        window.setTitle("Beer Keg Monitor");
        window.isFullScreen();
        //window.initStyle(StageStyle.UNDECORATED);

        Font fontLato = Font.loadFont(getClass()
                        .getResourceAsStream("/css/Lato-Hairline.ttf"), 80);

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

        Polygon kegMeter = temperatureData.createWeightMeter();
        kegMeter.setFill(Color.web("06d3ce"));

        Label kegVolume = temperatureData.createWeightLabel();
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

        Label temperature = temperatureData.createTempLabel();
        temperature.setTextFill(Color.web("cacaca"));
        temperature.setFont(fontLato);

        BorderPane tempFrame = new BorderPane();
        tempFrame.setPrefSize(715,270);
        //tempFrame.setMaxWidth(715);
        tempFrame.setStyle("-fx-background-color: #2d2d2d");
        tempFrame.setTop(tempheader);
        tempFrame.setCenter(temperatureData.createChart());
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
        //votingFrame.setMaxWidth(715);
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
        //footerFrame.setMaxWidth(1190);
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

        Scene scene = new Scene(root, 1280, 1024);
        scene.getStylesheets().add("css/linechart.css");
        window.setScene(scene);
        window.show();
    }
}
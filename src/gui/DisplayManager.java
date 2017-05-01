package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.text.Text;
import tools.Util;

/**
 * Created by rchen on 2017-04-29.
 */

public class DisplayManager {

    private final Map<String,ImageView> images;
    private final BorderPane kegFrame, tempFrame, votingFrame, footerFrame;

    DisplayManager() {
        images = new HashMap<>();
        kegFrame = new BorderPane();
        tempFrame = new BorderPane();
        votingFrame = new BorderPane();
        footerFrame = new BorderPane();

        loadFonts();
        loadImages();
    }

    public ImageView getImage(String imgName) { return images.get(imgName); }

    public BorderPane getKegFrame() { return kegFrame;  }

    public BorderPane getTempFrame() { return tempFrame; }

    public BorderPane getVotingFrame() { return votingFrame; }

    public BorderPane getFooterFrame() { return footerFrame; }

    public void createKegLayout(List<Node> elements) {
        Node kegMeter = elements.get(0);
        Node kegVolume = elements.get(1);

        kegMeter.getStyleClass().add("keg-meter");
        kegVolume.getStyleClass().add("data-labels");
        kegFrame.getStyleClass().addAll("all-frames", "keg-frame");

        StackPane kegMeterStack = new StackPane(kegMeter,kegVolume);
        StackPane.setAlignment(kegVolume, Pos.BOTTOM_LEFT);
        StackPane.setMargin(kegVolume, new Insets(0, 0, 20, 60));
        StackPane.setAlignment(kegMeter, Pos.BOTTOM_LEFT);
        StackPane.setMargin(kegMeter, new Insets(0, 0, 43, 5));

        kegFrame.setPrefSize(445, 450);
        kegFrame.setTop(getImage("kegheader"));
        kegFrame.setLeft(getImage("keg"));
        kegFrame.setCenter(kegMeterStack);

        BorderPane.setAlignment(getImage("keg"), Pos.BOTTOM_LEFT);
        BorderPane.setMargin(getImage("keg"), new Insets(15, 10, 15, 30));
    }

    public void createTempLayout(List<Node> elements) {
        Node temperature = elements.get(0);
        Node tempChart = elements.get(1);

        temperature.getStyleClass().add("data-labels");
        tempFrame.getStyleClass().addAll("all-frames", "temp-frame");

        tempFrame.setPrefSize(715, 270);
        tempFrame.setMaxWidth(715);
        tempFrame.setTop(getImage("tempheader"));
        tempFrame.setCenter(tempChart);
        tempFrame.setRight(temperature);

        BorderPane.setAlignment(temperature, Pos.TOP_RIGHT);
        BorderPane.setMargin(temperature, new Insets(5, 30, 0, 0));
    }

    public void createVotingLayout(List<Node> elements) {
        Text pressToVote = (Text) elements.get(0);
        Text pleaseScanCard = (Text) elements.get(1);
        Button addButton = (Button) elements.get(2);
        Button left = (Button) elements.get(3);
        Button right = (Button) elements.get(4);
        Button like = (Button) elements.get(5);
        Text beerDisplay = (Text) elements.get(6);
        Text likesDisplay = (Text) elements.get(7);
        HBox pollChart = (HBox) elements.get(8);

        StackPane votingHeader = new StackPane();
        BorderPane navPane = new BorderPane();
        VBox likePane = new VBox();

        pressToVote.getStyleClass().add("press-to-vote");
        pleaseScanCard.getStyleClass().add("votes-display");
        votingFrame.getStyleClass().addAll("all-frames", "voting-frame");

        addButton.setGraphic(getImage("add"));
        addButton.setBackground(Background.EMPTY);
        left.setGraphic(getImage("navleft1"));
        left.setBackground(Background.EMPTY);
        right.setGraphic(getImage("navright1"));
        right.setBackground(Background.EMPTY);
        like.setGraphic(getImage("like"));
        like.setBackground(Background.EMPTY);

        votingHeader.getChildren().add(getImage("votingheader"));
        votingHeader.getChildren().add(addButton);
        votingHeader.getChildren().add(pleaseScanCard);
        StackPane.setAlignment(addButton, Pos.CENTER_RIGHT);
        StackPane.setMargin(addButton, new Insets(0, 15, 0, 0));

        votingFrame.setPrefSize(715, 150);
        votingFrame.setMaxWidth(715);

        navPane.setLeft(left);
        navPane.setRight(right);
        navPane.setCenter(beerDisplay);
        navPane.setPrefSize(490, 90);

        likePane.getChildren().add(pressToVote);
        likePane.getChildren().add(likesDisplay);
        likePane.alignmentProperty().set(Pos.CENTER);

        votingFrame.setTop(votingHeader);
        votingFrame.setRight(navPane);
        votingFrame.setCenter(like);
        votingFrame.setLeft(likePane);
        votingFrame.setBottom(pollChart);

        BorderPane.setAlignment(left, Pos.CENTER_LEFT);
        BorderPane.setAlignment(right, Pos.CENTER_RIGHT);
        BorderPane.setAlignment(like, Pos.CENTER_LEFT);
        BorderPane.setMargin(likePane, new Insets(0, 5, 0, 20));
        BorderPane.setMargin(navPane, new Insets(5, 10, 5, 10));
        BorderPane.setMargin(votingFrame.getBottom(), new Insets(5, 5, 20, 5));
    }

    public void createFooterLayout(List<Node> elements) {
        Label currentKeg = (Label) elements.get(0);
        Button adminSettings = (Button) elements.get(1);

        StackPane headerStack = new StackPane();

        adminSettings.setGraphic(getImage("settings"));
        adminSettings.setBackground(Background.EMPTY);

        headerStack.getChildren().addAll(getImage("footerheader"), adminSettings);
        StackPane.setAlignment(adminSettings, Pos.CENTER_RIGHT);
        StackPane.setMargin(adminSettings, new Insets(0, 15, 0, 0));

        currentKeg.getStyleClass().add("data-labels");

        footerFrame.getStyleClass().addAll("all-frames", "footer-frame");
        footerFrame.setPrefWidth(1190);
        footerFrame.setMaxWidth(1190);
        footerFrame.setTop(headerStack);
        footerFrame.setLeft(currentKeg);
        footerFrame.setRight(getImage("teralogo"));

        BorderPane.setAlignment(currentKeg, Pos.BOTTOM_LEFT);
        BorderPane.setMargin(currentKeg, new Insets(5, 0, 0, 14));
        BorderPane.setAlignment(getImage("teralogo"), Pos.BOTTOM_RIGHT);
        BorderPane.setMargin(getImage("teralogo"), new Insets(5, 0, 15, 0));
    }

    private void loadFonts() {
        Font.loadFont(getClass().getResourceAsStream("/css/Lato-Hairline.ttf"), 80);
        Font.loadFont(getClass().getResourceAsStream("/css/Lato-Light.ttf"), 20);
    }

    private void loadImages() {
        images.put("kegheader",Util.importImage("img/kegheader.png", 53));
        images.put("tempheader",Util.importImage("img/tempheader.png", 53));
        images.put("votingheader",Util.importImage("img/votingheader.png", 53));
        images.put("footerheader",Util.importImage("img/footerheader.png", 53));
        images.put("navleft1",Util.importImage("img/navleft.png", 60));
        images.put("navleft2",Util.importImage("img/navleft.png", 60));
        images.put("navright1",Util.importImage("img/navright.png", 60));
        images.put("navright2",Util.importImage("img/navright.png", 60));
        images.put("teralogo",Util.importImage("img/teralogo.png", 146));
        images.put("settings",Util.importImage("img/settings.png", 30));
        images.put("keg",Util.importImage("img/keg.png", 345));
        images.put("like",Util.importImage("img/like.png", 60));
        images.put("add",Util.importImage("img/add.png", 30));
        images.put("close",Util.importImage("img/close.png", 25));
        images.put("plus",Util.importImage("img/plus.png", 20));
        images.put("minus",Util.importImage("img/minus.png", 25));
    }
}

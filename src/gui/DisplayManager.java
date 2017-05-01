package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void createKegFrame(List<Node> elements) {
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

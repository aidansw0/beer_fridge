package gui;

import javafx.scene.image.ImageView;
import javafx.scene.text.Font;

import java.util.HashMap;
import java.util.Map;

import tools.Util;

/**
 * Created by rchen on 2017-04-29.
 */

public class DisplayManager {

    private final Map<String,ImageView> images;

    DisplayManager() {
        images = new HashMap<>();

        loadFonts();
        loadImages();
    }

    public ImageView getImage(String imgName) { return images.get(imgName); }

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

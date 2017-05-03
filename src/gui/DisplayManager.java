package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.text.Text;
import tools.Util;

/**
 * This class manages the layout of the GUI.
 * It manages fonts, images, and applying style sheets to
 * all elements drawn on the main scene.
 *
 * @author Richard
 */

public class DisplayManager {

    private final Map<String,ImageView> images = new HashMap<>();
    private BorderPane kegFrame, tempFrame, votingFrame, tempAndVotingFrame, footerFrame, keyboardFrame, root;
    private StackPane adminPanel;
    private boolean keyboardOn = false;

    DisplayManager() {
        loadFonts();
        loadImages();
    }

    /**
     * Get the status of the keyboard in the GUI
     *
     * @return true if keyboard is visible, else false
     */
    public boolean getKeyboardVisibleStatus() { return keyboardOn; }

    /**
     * Get the root node that contains all frames
     *
     * @return BorderPane root
     */
    public BorderPane getRoot() { return root; }

    /**
     * Get the node of the adminPanel
     *
     * @return StackPane adminPanel
     */
    public StackPane getAdminPanel() { return adminPanel; }

    /**
     * Toggles the keyboard and footer frame based and toggles
     * the boolean status variable keyboardOn
     *
     * @return true if keyboard is visible, else false
     */
    public void toggleFooter() {
        if (keyboardOn) {
            root.setBottom(footerFrame);
            keyboardOn = !keyboardOn;
        } else {
            root.setBottom(keyboardFrame);
            keyboardOn = !keyboardOn;
        }
    }

    /**
     * Creates the layout of the keg frame
     *
     * @param elements, a list of all nodes to be placed in the frame
     */
    public void createKegLayout(List<Node> elements) {
        kegFrame = new BorderPane();

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
        kegFrame.setTop(images.get("kegheader"));
        kegFrame.setLeft(images.get("keg"));
        kegFrame.setCenter(kegMeterStack);

        BorderPane.setAlignment(images.get("keg"), Pos.BOTTOM_LEFT);
        BorderPane.setMargin(images.get("keg"), new Insets(15, 10, 15, 30));
    }

    /**
     * Creates the layout of the temp frame
     *
     * @param elements, a list of all nodes to be placed in the frame
     * @return the tempFrame node
     */
    public BorderPane createTempLayout(List<Node> elements) {
        tempFrame = new BorderPane();

        Node temperature = elements.get(0);
        Node tempChart = elements.get(1);

        temperature.getStyleClass().add("data-labels");
        tempFrame.getStyleClass().addAll("all-frames", "temp-frame");

        tempFrame.setPrefSize(715, 270);
        tempFrame.setMaxWidth(715);
        tempFrame.setTop(images.get("tempheader"));
        tempFrame.setCenter(tempChart);
        tempFrame.setRight(temperature);

        BorderPane.setAlignment(temperature, Pos.TOP_RIGHT);
        BorderPane.setMargin(temperature, new Insets(5, 30, 0, 0));

        return tempFrame;
    }

    /**
     * Creates the layout of the voting frame
     *
     * @param elements, a list of all nodes to be placed in the frame
     * @return the votingFrame node
     */
    public BorderPane createVotingLayout(List<Node> elements) {
        votingFrame = new BorderPane();

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

        addButton.setGraphic(images.get("add"));
        addButton.setBackground(Background.EMPTY);
        left.setGraphic(images.get("navleft1"));
        left.setBackground(Background.EMPTY);
        right.setGraphic(images.get("navright1"));
        right.setBackground(Background.EMPTY);
        like.setGraphic(images.get("like"));
        like.setBackground(Background.EMPTY);

        votingHeader.getChildren().add(images.get("votingheader"));
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

        return votingFrame;
    }

    /**
     * Creates the layout of the temp and voting frame combined so
     * it can be used as the right pane of a BorderPane node
     *
     * @param elements, a list of all nodes to be placed in the frame
     */
    public void createTempAndVotingLayout(List<Node> elements) {
        tempAndVotingFrame = new BorderPane();
        tempAndVotingFrame.getStyleClass().add("temp-voting-frame");
        tempAndVotingFrame.setPrefSize(715, 450);
        tempAndVotingFrame.setTop(tempFrame);
        tempAndVotingFrame.setCenter(votingFrame);

        BorderPane.setAlignment(votingFrame, Pos.TOP_LEFT);
        BorderPane.setAlignment(tempFrame, Pos.TOP_LEFT);
    }

    /**
     * Creates the layout of the footer frame
     *
     * @param elements, a list of all nodes to be placed in the frame
     */
    public void createFooterLayout(List<Node> elements) {
        footerFrame = new BorderPane();

        Label currentKeg = (Label) elements.get(0);
        Button adminSettings = (Button) elements.get(1);

        StackPane headerStack = new StackPane();

        adminSettings.setGraphic(images.get("settings"));
        adminSettings.setBackground(Background.EMPTY);

        headerStack.getChildren().addAll(images.get("footerheader"), adminSettings);
        StackPane.setAlignment(adminSettings, Pos.CENTER_RIGHT);
        StackPane.setMargin(adminSettings, new Insets(0, 15, 0, 0));

        currentKeg.getStyleClass().add("data-labels");

        footerFrame.getStyleClass().addAll("all-frames", "footer-frame");
        footerFrame.setPrefWidth(1190);
        footerFrame.setMaxWidth(1190);
        footerFrame.setTop(headerStack);
        footerFrame.setLeft(currentKeg);
        footerFrame.setRight(images.get("teralogo"));

        BorderPane.setAlignment(currentKeg, Pos.BOTTOM_LEFT);
        BorderPane.setMargin(currentKeg, new Insets(5, 0, 0, 14));
        BorderPane.setAlignment(images.get("teralogo"), Pos.BOTTOM_RIGHT);
        BorderPane.setMargin(images.get("teralogo"), new Insets(5, 0, 15, 0));
    }

    /**
     * Creates the layout of the keg frame
     *
     * @param element, the virtual keyboard node
     */
    public void createKeyboardLayout(Node element) {
        keyboardFrame = new BorderPane();

        keyboardFrame.setCenter(element);
        keyboardFrame.setPrefWidth(1190);
        keyboardFrame.setMaxWidth(1190);

        BorderPane.setAlignment(keyboardFrame, Pos.TOP_LEFT);
        BorderPane.setMargin(keyboardFrame, new Insets(15, 50, 45, 50));
    }

    /**
     * Creates the layout of the main GUI. This method creates the GUI
     * from the global nodes under the assumption that they have already
     * been created using the above methods. The class calling this createRoot()
     * is responsible for creating the frames needed: kegFrame, tempFrame, votingFrame,
     * and footerFrame.
     */
    public BorderPane createRoot() {
        root = new BorderPane();
        root.setPrefSize(1190, 450);
        root.getStyleClass().add("main-window");
        root.setLeft(kegFrame);
        root.setCenter(tempAndVotingFrame);
        root.setBottom(footerFrame);

        BorderPane.setAlignment(root.getLeft(), Pos.CENTER_RIGHT);
        BorderPane.setAlignment(root.getCenter(), Pos.CENTER_LEFT);
        BorderPane.setAlignment(root.getBottom(), Pos.TOP_LEFT);

        return root;
    }

    /**
     * Creates the layout of the admin panel
     *
     * @param elements, a list of all nodes to be placed in the frame
     */
    public void createAdminPanelLayout(List<Node> elements) {
        adminPanel = new StackPane();

        // assign the element nodes to variables for readability
        Button left = (Button) elements.get(0);
        Button right = (Button) elements.get(1);
        Text beerDisplay = (Text) elements.get(2);
        Text likesDisplay = (Text) elements.get(3);
        Button closeButton = (Button) elements.get(4);
        Button resetVote = (Button) elements.get(5);
        Button delete = (Button) elements.get(6);
        Button setToCurrent = (Button) elements.get(7);
        Button kegTapped = (Button) elements.get(8);
        Button newAdmin = (Button) elements.get(9);
        Button removeAdmin = (Button) elements.get(10);
        Button thirtyLitre = (Button) elements.get(11);
        Button fiftyLitre = (Button) elements.get(12);
        Button cancel = (Button) elements.get(13);
        Button plus = (Button) elements.get(14);
        Button minus = (Button) elements.get(15);
        Label tareLabel = (Label) elements.get(16);
        VBox newKeggedTapped = (VBox) elements.get(17);
        VBox adminPopup = (VBox) elements.get(18);
        VBox buttonList = (VBox) elements.get(19);

        BorderPane navPane = new BorderPane();
        VBox beerDisplayBox = new VBox();
        StackPane header = new StackPane();
        Rectangle adminContainer = new Rectangle(620,650);

        setToCurrent.getStyleClass().add("admin-button");
        delete.getStyleClass().add("admin-button");
        resetVote.getStyleClass().add("admin-button");
        kegTapped.getStyleClass().add("admin-button");
        newAdmin.getStyleClass().add("admin-button");
        removeAdmin.getStyleClass().add("admin-button");
        thirtyLitre.getStyleClass().add("admin-button");
        fiftyLitre.getStyleClass().add("admin-button");
        cancel.getStyleClass().add("admin-button");

        closeButton.setGraphic(images.get("close"));
        closeButton.setBackground(Background.EMPTY);
        plus.setGraphic(images.get("plus"));
        plus.setBackground(Background.EMPTY);
        minus.setGraphic(images.get("minus"));
        minus.setBackground(Background.EMPTY);
        left.setGraphic(images.get("navleft2"));
        left.setBackground(Background.EMPTY);
        right.setGraphic(images.get("navright2"));
        right.setBackground(Background.EMPTY);

        adminContainer.setFill(Color.web("1e1e1e"));
        adminContainer.setOpacity(0.9);

        beerDisplayBox.setAlignment(Pos.CENTER);
        beerDisplayBox.getChildren().addAll(beerDisplay,likesDisplay);

        navPane.setLeft(left);
        navPane.setCenter(beerDisplayBox);
        navPane.setRight(right);
        navPane.setMaxWidth(570);

        resetVote.setMinWidth(500);
        delete.setMinWidth(500);
        setToCurrent.setMinWidth(500);
        kegTapped.setMinWidth(500);
        newAdmin.setMinWidth(500);
        removeAdmin.setMinWidth(500);

        header.setAlignment(Pos.CENTER_RIGHT);
        header.getChildren().addAll(closeButton);
        header.setPrefHeight(70);

        buttonList.setAlignment(Pos.CENTER);
        buttonList.getChildren().addAll(setToCurrent,delete,resetVote,kegTapped,newAdmin,removeAdmin);
        adminPopup.setMaxWidth(600);
        adminPopup.setMaxHeight(650);
        adminPopup.setAlignment(Pos.TOP_CENTER);
        adminPopup.getChildren().addAll(header, navPane, buttonList);

        HBox kegSizeButtons = new HBox(thirtyLitre,fiftyLitre,cancel);
        kegSizeButtons.setAlignment(Pos.CENTER);
        kegSizeButtons.setSpacing(30);

        Text line1 = new Text("\n" +
                "1. Scroll through the beer list to choose the new keg\n" +
                "2. Select the correct keg size\n\n" +
                "Please make sure the keg is already tapped before selecting the keg size so that the scale can tare correctly. " +
                "This will also reset all the votes and allow users to vote again.");
        line1.getStyleClass().add("admin-warning-msg");
        line1.setWrappingWidth(550);

        Text manualAdjust = new Text("\nManually adjust current keg level");
        manualAdjust.getStyleClass().add("admin-warning-msg");

        HBox kegAdjustButtons = new HBox(minus,tareLabel,plus);
        kegAdjustButtons.setAlignment(Pos.CENTER);
        kegAdjustButtons.setSpacing(30);

        newKeggedTapped.setMaxWidth(600);
        newKeggedTapped.setAlignment(Pos.TOP_CENTER);
        newKeggedTapped.getChildren().addAll(kegSizeButtons,line1,manualAdjust,kegAdjustButtons);

        BorderPane.setAlignment(left, Pos.CENTER_LEFT);
        BorderPane.setAlignment(right, Pos.CENTER_RIGHT);

        adminPanel.getChildren().addAll(adminContainer, adminPopup);
    }

    /**
     * Used by the constructor to load all fonts used in the GUI
     */
    private void loadFonts() {
        Font.loadFont(getClass().getResourceAsStream("/css/Lato-Hairline.ttf"), 80);
        Font.loadFont(getClass().getResourceAsStream("/css/Lato-Light.ttf"), 20);
    }

    /**
     * Used by the constructor to load all images used in the GUI
     */
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

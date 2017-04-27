package gui;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;

import backend.KegManager;
import backend.KeyCardListener;
import backend.SaveData;
import backend.VirtualKeyboard;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class Main extends Application {

    private final KegManager beerKeg = new KegManager();
    private final DataManager dataManager = new DataManager(beerKeg);

    private final SaveData saveData = new SaveData();
    private final VoteManager voteManager = new VoteManager(saveData);
    private final KeyCardListener keyCardListener = new KeyCardListener(saveData);

    private static final long WRITE_DATA_PERIOD = 600000; // in ms

    private final Label newBeerField = new Label();
    private Label currentKeg;
    private Stage window;
    private BorderPane kegFrame, tempAndVotingFrame, footerFrame, keyboardFrame, root;
    private StackPane adminPanel, finalStack; //asda

    private double initial = 0;

    private boolean keyboardOn = false;
    private boolean adminPanelOn = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        init(primaryStage);
        createScene();
    }

    private void init(Stage primaryStage) {
        window = primaryStage;
        window.setTitle("Beer Keg Monitor");
        window.isFullScreen();

        Font.loadFont(getClass().getResourceAsStream("/css/Lato-Hairline.ttf"), 80);
        Font.loadFont(getClass().getResourceAsStream("/css/Lato-Light.ttf"), 20);

        // setup timer task to write beer/user data to file periodically
        Timer saveTimer = new Timer();
        TimerTask save = new TimerTask() {

            @Override
            public void run() {
                voteManager.saveBeerData();
                try {
                    saveData.writeCurrentKeg(currentKeg.getText(), dataManager.getTare());
                    saveData.writeUsersToFile();
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        };

        saveTimer.schedule(save, WRITE_DATA_PERIOD, WRITE_DATA_PERIOD);

        primaryStage.setOnCloseRequest(event -> {
            try {
                voteManager.saveBeerData();
                saveData.writeUsersToFile();
                saveData.writeCurrentKeg(currentKeg.getText(), dataManager.getTare());
            } catch (Exception e) {
                e.printStackTrace();
            }

            Platform.exit();
            System.exit(0);
        });
    }

    private void createKegFrame() {
        kegFrame = new BorderPane();
        StackPane kegMeterStack = new StackPane();
        Polygon kegMeter = dataManager.createWeightMeter();
        Label kegVolume = dataManager.createWeightLabel();

        // Import images
        ImageView keg = importImage("img/keg.png", 345);
        ImageView kegheader = importImage("img/kegheader.png", 53);

        kegMeter.getStyleClass().add("keg-meter");
        kegVolume.getStyleClass().add("data-labels");

        kegMeterStack.getChildren().add(kegMeter);
        kegMeterStack.getChildren().add(kegVolume);

        StackPane.setAlignment(kegVolume, Pos.BOTTOM_LEFT);
        StackPane.setMargin(kegVolume, new Insets(0, 0, 20, 60));
        StackPane.setAlignment(kegMeter, Pos.BOTTOM_LEFT);
        StackPane.setMargin(kegMeter, new Insets(0, 0, 43, 5));

        kegFrame.getStyleClass().addAll("all-frames", "keg-frame");
        kegFrame.setPrefSize(445, 450);
        kegFrame.setTop(kegheader);
        kegFrame.setLeft(keg);
        kegFrame.setCenter(kegMeterStack);

        BorderPane.setAlignment(keg, Pos.BOTTOM_LEFT);
        BorderPane.setMargin(keg, new Insets(15, 10, 15, 30));
    }

    private BorderPane createTempFrame() {
        BorderPane tempFrame = new BorderPane();
        Label temperature = dataManager.createTempLabel();
        ImageView tempheader = importImage("img/tempheader.png", 53);

        temperature.getStyleClass().add("data-labels");

        tempFrame.getStyleClass().addAll("all-frames", "temp-frame");
        tempFrame.setPrefSize(715, 270);
        tempFrame.setMaxWidth(715);
        tempFrame.setTop(tempheader);
        tempFrame.setCenter(dataManager.createLineChart());
        tempFrame.setRight(temperature);

        BorderPane.setAlignment(temperature, Pos.TOP_RIGHT);
        BorderPane.setMargin(temperature, new Insets(5, 30, 0, 0));

        return tempFrame;
    }

    private BorderPane createVotingFrame() {
        StackPane votingHeader = new StackPane();
        BorderPane votingFrame = new BorderPane();
        BorderPane navPane = new BorderPane();
        VBox likePane = new VBox();
        Text pressToVote = new Text("Press to Vote");
        Text pleaseScanCard = new Text("Please scan card ...");
        Button addButton = new Button();

        // Import images
        ImageView navleft = importImage("img/navleft.png", 60);
        ImageView navright = importImage("img/navright.png", 60);
        ImageView votingheaderimg = importImage("img/votingheader.png", 53);
        ImageView thumb = importImage("img/like.png", 60);
        ImageView plusimg = importImage("img/add.png", 30);

        newBeerField.getStyleClass().add("new-beer-field");
        newBeerField.setPrefWidth(715);
        newBeerField.setOnKeyPressed((KeyEvent event) -> keyboardEvents(event, votingFrame));

        addButton.setGraphic(plusimg);
        addButton.setBackground(Background.EMPTY);
        addButton.disableProperty().bind(keyCardListener.adminKeyVerifiedProperty().not());
        addButton.setOnAction(event -> toggleAdminKeyboard(votingFrame));

        votingHeader.getChildren().add(votingheaderimg);
        votingHeader.getChildren().add(addButton);
        votingHeader.getChildren().add(pleaseScanCard);
        StackPane.setAlignment(addButton, Pos.CENTER_RIGHT);
        StackPane.setMargin(addButton, new Insets(0, 15, 0, 0));

        Button left = voteManager.createLeftButton(navleft);
        Button right = voteManager.createRightButton(navright);
        Button like = voteManager.createLikeButton(thumb, keyCardListener);
        like.disableProperty().bind(keyCardListener.regularKeyVerifiedProperty().not());

        pressToVote.getStyleClass().add("press-to-vote");
        pleaseScanCard.getStyleClass().add("votes-display");
        pleaseScanCard.textProperty().bind(keyCardListener.getHintText());

        votingFrame.getStyleClass().addAll("all-frames", "voting-frame");
        votingFrame.setPrefSize(715, 150);
        votingFrame.setMaxWidth(715);

        votingFrame.onMousePressedProperty().set(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                event.setDragDetect(true);
                initial = event.getX();
            }
        });

        votingFrame.onMouseReleasedProperty().set(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getX() < initial - 100) {
                    voteManager.swipeLeft();
                } else if (event.getX() > initial + 100) {
                    voteManager.swipeRight();
                }
            }
        });

        navPane.setLeft(left);
        navPane.setRight(right);
        navPane.setCenter(voteManager.createBeerDisplay());
        navPane.setPrefSize(490, 90);

        likePane.getChildren().add(pressToVote);
        likePane.getChildren().add(voteManager.createLikesDisplay());
        likePane.alignmentProperty().set(Pos.CENTER);

        votingFrame.setTop(votingHeader);
        votingFrame.setRight(navPane);
        votingFrame.setCenter(like);
        votingFrame.setLeft(likePane);
        votingFrame.setBottom(voteManager.createPollChart());

        BorderPane.setAlignment(left, Pos.CENTER_LEFT);
        BorderPane.setAlignment(right, Pos.CENTER_RIGHT);
        BorderPane.setAlignment(like, Pos.CENTER_LEFT);
        BorderPane.setMargin(likePane, new Insets(0, 5, 0, 20));
        BorderPane.setMargin(navPane, new Insets(5, 10, 5, 10));
        BorderPane.setMargin(votingFrame.getBottom(), new Insets(5, 5, 20, 5));

        return votingFrame;
    }

    private void createTempAndVotingFrame() {
        BorderPane tempFrame = createTempFrame();
        BorderPane votingFrame = createVotingFrame();

        tempAndVotingFrame = new BorderPane();
        tempAndVotingFrame.getStyleClass().add("temp-voting-frame");
        tempAndVotingFrame.setPrefSize(715, 450);
        tempAndVotingFrame.setTop(tempFrame);
        tempAndVotingFrame.setCenter(votingFrame);

        BorderPane.setAlignment(votingFrame, Pos.TOP_LEFT);
        BorderPane.setAlignment(tempFrame, Pos.TOP_LEFT);
    }

    private void createFooterFrame() {
        footerFrame = new BorderPane();
        currentKeg = new Label("Steamworks Kolsch");
        StackPane headerStack = new StackPane();
        Button adminSettings = new Button();

        // get current keg data from saveData
        Object[] keg = saveData.readCurrentKeg();
        currentKeg.setText((String) keg[0]);
        dataManager.setTare((double) keg[1]);

        // Import images
        ImageView footerheader = importImage("img/footerheader.png", 53);
        ImageView teralogo = importImage("img/teralogo.png", 146);
        ImageView settingsimg = importImage("img/settings.png", 30);

        adminSettings.setGraphic(settingsimg);
        adminSettings.setBackground(Background.EMPTY);
        adminSettings.disableProperty().bind(keyCardListener.adminKeyVerifiedProperty().not());
        adminSettings.setOnAction(event -> toggleAdminPanel());

        headerStack.getChildren().addAll(footerheader, adminSettings);
        StackPane.setAlignment(adminSettings, Pos.CENTER_RIGHT);
        StackPane.setMargin(adminSettings, new Insets(0, 15, 0, 0));

        currentKeg.getStyleClass().add("data-labels");

        footerFrame.getStyleClass().addAll("all-frames", "footer-frame");
        footerFrame.setPrefWidth(1190);
        footerFrame.setMaxWidth(1190);
        footerFrame.setTop(headerStack);
        footerFrame.setLeft(currentKeg);
        footerFrame.setRight(teralogo);

        BorderPane.setAlignment(currentKeg, Pos.BOTTOM_LEFT);
        BorderPane.setMargin(currentKeg, new Insets(5, 0, 0, 14));
        BorderPane.setAlignment(teralogo, Pos.BOTTOM_RIGHT);
        BorderPane.setMargin(teralogo, new Insets(5, 0, 15, 0));
    }

    private void createScene() {
        createKegFrame();
        createTempAndVotingFrame();
        createFooterFrame();
        createKeyboardPopUp();
        createAdminPanel();

        root = new BorderPane();
        root.setPrefSize(1190, 450);
        root.getStyleClass().add("main-window");
        root.setLeft(kegFrame);
        root.setCenter(tempAndVotingFrame);
        root.setBottom(footerFrame);
        root.setOnKeyPressed((KeyEvent event) -> keyCardListener.handleEvent(event));

        finalStack = new StackPane();
        finalStack.getChildren().addAll(root);

        BorderPane.setAlignment(kegFrame, Pos.CENTER_RIGHT);
        BorderPane.setAlignment(tempAndVotingFrame, Pos.CENTER_LEFT);
        BorderPane.setAlignment(footerFrame, Pos.TOP_LEFT);

        Scene scene = new Scene(finalStack, 1280, 1024);
        scene.getStylesheets().add("css/linechart.css");
        scene.getStylesheets().add("css/keyboard.css");
        scene.getStylesheets().add("css/main.css");

        // scene.setCursor(Cursor.NONE);
        // window.initStyle(StageStyle.UNDECORATED);
        window.setMaxWidth(1280);
        window.setMaxHeight(1024);

        window.setScene(scene);
        window.show();
    }

    private void createKeyboardPopUp() {
        VirtualKeyboard vkb = new VirtualKeyboard(newBeerField);
        Node keys = vkb.view();

        keyboardFrame = new BorderPane();
        keyboardFrame.setCenter(keys);
        keyboardFrame.setPrefWidth(1190);
        keyboardFrame.setMaxWidth(1190);

        BorderPane.setAlignment(keyboardFrame, Pos.TOP_LEFT);
        BorderPane.setMargin(keyboardFrame, new Insets(15, 50, 45, 50));
    }

    private void createAdminPanel() {
        adminPanel = new StackPane();
        BorderPane navPane = new BorderPane();
        VBox adminPopup = new VBox(30);
        VBox newKeggedTapped = new VBox(20);
        VBox beerDisplay = new VBox();
        StackPane header = new StackPane();
        VBox buttonList = new VBox(15);
        Rectangle adminContainer = new Rectangle(620, 500);

        adminContainer.setFill(Color.web("1e1e1e"));
        adminContainer.setOpacity(0.9);

        ImageView close = importImage("img/close.png", 25);
        ImageView navleft = importImage("img/navleft.png", 60);
        ImageView navright = importImage("img/navright.png", 60);

        Button closeButton = new Button();
        closeButton.setGraphic(close);
        closeButton.setBackground(Background.EMPTY);
        closeButton.setOnAction(event -> toggleAdminPanel());

        header.setAlignment(Pos.CENTER_RIGHT);
        header.getChildren().addAll(closeButton);
        header.setPrefHeight(70);

        beerDisplay.setAlignment(Pos.CENTER);
        beerDisplay.getChildren().addAll(voteManager.createBeerDisplay(), voteManager.createLikesDisplay());

        Button left = voteManager.createLeftButton(navleft);
        Button right = voteManager.createRightButton(navright);
        navPane.setLeft(left);
        navPane.setCenter(beerDisplay);
        navPane.setRight(right);
        navPane.setMaxWidth(570);

        Button resetVote = new Button("Reset All Votes");
        resetVote.setMinWidth(500);
        Button delete = new Button("Delete This Beer");
        delete.setMinWidth(500);
        Button setToCurrent = new Button("Set This Beer to the Current Keg");
        setToCurrent.setMinWidth(500);
        Button kegTapped = new Button("Tap a New Beer Keg");
        kegTapped.setMinWidth(500);

        delete.getStyleClass().add("admin-button");
        resetVote.getStyleClass().add("admin-button");
        setToCurrent.getStyleClass().add("admin-button");
        setToCurrent.setOnAction(event -> currentKeg.setText(voteManager.getCurrentBeer()));

        delete.setOnAction(event -> {
            System.out.println("Deletings current beer");
            voteManager.deleteCurrentBeer();
        });

        resetVote.setOnAction(event -> {
            System.out.println("Reseting votes");
            voteManager.resetVotes();
            saveData.resetVotes();
        });

        kegTapped.getStyleClass().add("admin-button");
        kegTapped.setOnAction(event -> {
            adminPopup.getChildren().remove(buttonList);
            adminPopup.getChildren().add(newKeggedTapped);
        });

        buttonList.setAlignment(Pos.CENTER);
        buttonList.getChildren().addAll(setToCurrent, delete, resetVote, kegTapped);
        adminPopup.setMaxWidth(600);
        adminPopup.setMaxHeight(500);
        adminPopup.setAlignment(Pos.TOP_CENTER);
        adminPopup.getChildren().addAll(header, navPane, buttonList);

        Button thirtyLitre = new Button("30L Keg");
        Button fiftyLitre = new Button("50L Keg");
        Button cancel = new Button("Cancel");
        thirtyLitre.setOnAction(event -> {
            dataManager.setMaxKegWeight(30);
            dataManager.tareToMaxWeight();
            currentKeg.setText(voteManager.getCurrentBeer());
            adminPopup.getChildren().remove(newKeggedTapped);
            adminPopup.getChildren().add(buttonList);
        });
        fiftyLitre.setOnAction(event -> {
            dataManager.setMaxKegWeight(50);
            dataManager.tareToMaxWeight();
            currentKeg.setText(voteManager.getCurrentBeer());
            adminPopup.getChildren().remove(newKeggedTapped);
            adminPopup.getChildren().add(buttonList);
        });

        thirtyLitre.getStyleClass().add("admin-button");
        fiftyLitre.getStyleClass().add("admin-button");
        cancel.getStyleClass().add("admin-button");
        cancel.setOnAction(event -> {
            adminPopup.getChildren().remove(newKeggedTapped);
            adminPopup.getChildren().add(buttonList);
        });
        HBox buttonRow2 = new HBox(thirtyLitre, fiftyLitre, cancel);
        buttonRow2.setAlignment(Pos.CENTER);
        buttonRow2.setSpacing(30);
        Text line1 = new Text(
                "\n1. Scroll through the beer list to choose the new keg\n" + "2. Select the correct keg size\n\n"
                        + "Please make sure the keg is already tapped before selecting the keg size so that the scale can tare correctly. "
                        + "This will also reset all the votes and allow users to vote again.");
        line1.getStyleClass().add("admin-warning-msg");
        line1.setWrappingWidth(550);
        newKeggedTapped.setMaxWidth(600);
        newKeggedTapped.setAlignment(Pos.TOP_CENTER);
        newKeggedTapped.getChildren().addAll(buttonRow2, line1);

        BorderPane.setAlignment(left, Pos.CENTER_LEFT);
        BorderPane.setAlignment(right, Pos.CENTER_RIGHT);

        adminPanel.getChildren().addAll(adminContainer, adminPopup);
    }

    private void toggleAdminPanel() {
        if (adminPanelOn) {
            finalStack.getChildren().remove(adminPanel);
            keyCardListener.checkAdminKeyVerified(true);
        } else {
            finalStack.getChildren().add(adminPanel);
        }
        adminPanelOn = !adminPanelOn;
    }

    private void toggleAdminKeyboard(BorderPane votingFrame) {
        if (keyboardOn) {
            keyCardListener.checkAdminKeyVerified(true);
            votingFrame.setBottom(voteManager.getPollChart());
            toggleKeyboard();
        } else {
            newBeerField.setText("");
            votingFrame.setBottom(newBeerField);
            newBeerField.requestFocus();
            toggleKeyboard();
        }
    }

    private void toggleKeyboard() {
        if (keyboardOn) {
            root.setBottom(footerFrame);
            keyboardOn = !keyboardOn;
        } else {
            root.setBottom(keyboardFrame);
            keyboardOn = !keyboardOn;
        }
    }

    private void keyboardEvents(KeyEvent event, BorderPane frame) {
        switch (event.getCode()) {
        case ENTER:
            if (!newBeerField.getText().isEmpty()) {
                if (voteManager.addBeer(newBeerField.getText(), 0)) {
                    keyCardListener.checkAdminKeyVerified(true);
                    frame.setBottom(voteManager.getPollChart());
                    toggleKeyboard();
                }
                newBeerField.setText("");
            }
            break;

        case ESCAPE:
            newBeerField.setText("");
            keyCardListener.checkAdminKeyVerified(true);
            frame.setBottom(voteManager.getPollChart());
            toggleKeyboard();
            break;
        }
    }

    private ImageView importImage(String imgPath, int fitHeight) {
        ImageView imgView = new ImageView(imgPath);
        imgView.setFitHeight(fitHeight);
        imgView.setPreserveRatio(true);

        return imgView;
    }
}
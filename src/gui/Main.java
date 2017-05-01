package gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import userInputs.KeyCardListener;
import userInputs.VirtualKeyboard;

import org.json.JSONException;

import dataManagement.DweetManager;
import dataManagement.DataManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application {

    private final DweetManager dweetManager = new DweetManager();
    private final KegManager kegManager = new KegManager(dweetManager);
    private final DataManager dataManager = new DataManager();
    private final VoteManager voteManager = new VoteManager(dataManager);
    private final DisplayManager displayManager = new DisplayManager();
    private final KeyCardListener keyCardListener = new KeyCardListener(dataManager);

    private static final long WRITE_DATA_PERIOD = 600000; // in ms

    private final Label newBeerField = new Label();
    private Stage window;
    private BorderPane kegFrame, tempAndVotingFrame, footerFrame, keyboardFrame, root;
    private StackPane adminPanel, finalStack;

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

        // setup timer task to write beer/user data to file periodically
        Timer saveTimer = new Timer();
        TimerTask save = new TimerTask() {

            @Override
            public void run() {
                voteManager.saveBeerData();
                try {
                    dataManager.writeCurrentKeg(voteManager.getCurrentKeg(), kegManager.getTare());
                    dataManager.writeUsersToFile();
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        };

        saveTimer.schedule(save, WRITE_DATA_PERIOD, WRITE_DATA_PERIOD);

        primaryStage.setOnCloseRequest(event -> {
            try {
                voteManager.saveBeerData();
                dataManager.writeUsersToFile();
                dataManager.writeCurrentKeg(voteManager.getCurrentKeg(), kegManager.getTare());
            } catch (Exception e) {
                e.printStackTrace();
            }

            Platform.exit();
            System.exit(0);
        });
    }

    private void createKegFrame() {
        List<Node> elements = new ArrayList<>();
        elements.add(kegManager.createWeightMeter());
        elements.add(kegManager.createWeightLabel());

        displayManager.createKegLayout(elements);
        kegFrame = displayManager.getKegFrame();
    }

    private BorderPane createTempFrame() {
        List<Node> elements = new ArrayList<>();
        elements.add(kegManager.createTempLabel());
        elements.add(kegManager.createLineChart());

        displayManager.createTempLayout(elements);
        return displayManager.getTempFrame();
    }

    private BorderPane createVotingFrame() {
        List<Node> elements = new ArrayList<>();
        elements.add(new Text("Press to Vote"));
        elements.add(new Text("Please scan card ..."));
        elements.add(new Button());
        elements.add(voteManager.createLeftButton());
        elements.add(voteManager.createRightButton());
        elements.add(voteManager.createLikeButton(keyCardListener));
        elements.add(voteManager.createBeerDisplay());
        elements.add(voteManager.createLikesDisplay());
        elements.add(voteManager.createPollChart());

        displayManager.createVotingLayout(elements);
        BorderPane votingFrame = displayManager.getVotingFrame();

        ((Text) elements.get(1)).textProperty().bind(keyCardListener.getHintText());
        ((Button) elements.get(2)).setOnAction(event -> toggleAdminKeyboard(votingFrame));
        elements.get(2).disableProperty().bind(keyCardListener.adminKeyVerifiedProperty().not());
        elements.get(5).disableProperty().bind(keyCardListener.regularKeyVerifiedProperty().not());

        newBeerField.getStyleClass().add("new-beer-field");
        newBeerField.setPrefWidth(715);
        newBeerField.setOnKeyPressed((KeyEvent event) -> keyboardEvents(event, votingFrame));

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
        List<Node> elements = new ArrayList<>();
        elements.add(new Label());
        elements.add(new Button());

        displayManager.createFooterLayout(elements);

        ((Label) elements.get(0)).textProperty().bind(voteManager.currentKegProperty());
        ((Button) elements.get(1)).setOnAction(event -> toggleAdminPanel());
        ((Button) elements.get(1)).disableProperty().bind(keyCardListener.adminKeyVerifiedProperty().not());

        // get current keg data from saveData
        Object[] keg = dataManager.readCurrentKeg();
        voteManager.setCurrentKeg((String) keg[0]);
        kegManager.setTare((double) keg[1]);

        footerFrame = displayManager.getFooterFrame();
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

//        scene.setCursor(Cursor.NONE);
//        window.initStyle(StageStyle.UNDECORATED);
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
        Rectangle adminContainer = new Rectangle(620,650);

        adminContainer.setFill(Color.web("1e1e1e"));
        adminContainer.setOpacity(0.9);

        beerDisplay.setAlignment(Pos.CENTER);
        beerDisplay.getChildren().addAll(voteManager.createBeerDisplay(), voteManager.createLikesDisplay());

        Button left = voteManager.createLeftButton();
        Button right = voteManager.createRightButton();
        navPane.setLeft(left);
        navPane.setCenter(beerDisplay);
        navPane.setRight(right);
        navPane.setMaxWidth(570);

        Button closeButton = new Button();
        Button resetVote = new Button("Reset All Votes");
        resetVote.setMinWidth(500);
        Button delete = new Button("Delete This Beer");
        delete.setMinWidth(500);
        Button setToCurrent = new Button("Set This Beer to the Current Keg");
        setToCurrent.setMinWidth(500);
        Button kegTapped = new Button("Tap a New Beer Keg");
        kegTapped.setMinWidth(500);
        Button newAdmin = new Button("Add a New Admin");
        newAdmin.setMinWidth(500);
        Button removeAdmin = new Button("Remove an Admin");
        removeAdmin.setMinWidth(500);

        closeButton.setGraphic(displayManager.getImage("close"));
        closeButton.setBackground(Background.EMPTY);
        closeButton.setOnAction(event -> {
            if (adminPopup.getChildren().contains(newKeggedTapped)) {
                adminPopup.getChildren().remove(newKeggedTapped);
                adminPopup.getChildren().add(buttonList);
            }
            newAdmin.textProperty().unbind();
            newAdmin.setText("Add a New Admin");
            removeAdmin.textProperty().unbind();
            removeAdmin.setText("Remove an Admin");

            // Save data
            try {
                voteManager.saveBeerData();
                dataManager.writeUsersToFile();
                dataManager.writeCurrentKeg(voteManager.getCurrentKeg(), kegManager.getTare());
            } catch (Exception e) {
                e.printStackTrace();
            }

            toggleAdminPanel();
        });

        setToCurrent.getStyleClass().add("admin-button");
        setToCurrent.setOnAction(event -> voteManager.setCurrentKeg(voteManager.getCurrentBeer()));

        delete.getStyleClass().add("admin-button");
        delete.setOnAction(event -> {
            System.out.println("Deleting current beer");
            voteManager.deleteCurrentBeer();
        });

        resetVote.getStyleClass().add("admin-button");
        resetVote.setOnAction(event -> {
            System.out.println("Resetting votes");
            voteManager.resetVotes();
            dataManager.resetVotes();
        });

        kegTapped.getStyleClass().add("admin-button");
        kegTapped.setOnAction(event -> {
            adminPopup.getChildren().remove(buttonList);
            adminPopup.getChildren().add(newKeggedTapped);
        });

        newAdmin.getStyleClass().add("admin-button");
        newAdmin.setOnAction(event -> {
            removeAdmin.setOnKeyPressed(null);
            removeAdmin.textProperty().unbind();
            keyCardListener.resetAdminHint();
            newAdmin.textProperty().bind(keyCardListener.getAdminHintText());
            newAdmin.setOnKeyPressed((KeyEvent keyEvent) -> {
                if(keyCardListener.handleNewAdmin(keyEvent)) {
                    newAdmin.textProperty().unbind();
                    newAdmin.setOnKeyPressed(null);
                }
            });
        });

        removeAdmin.getStyleClass().add("admin-button");
        removeAdmin.setOnAction(event -> {
            newAdmin.setOnKeyPressed(null);
            newAdmin.textProperty().unbind();
            keyCardListener.resetAdminHint();
            removeAdmin.textProperty().bind(keyCardListener.getAdminHintText());
            removeAdmin.setOnKeyPressed((KeyEvent keyEvent) -> {
                if(keyCardListener.handleRemoveAdmin(keyEvent)) {
                    removeAdmin.textProperty().unbind();
                    removeAdmin.setOnKeyPressed(null);
                }
            });
        });

        header.setAlignment(Pos.CENTER_RIGHT);
        header.getChildren().addAll(closeButton);
        header.setPrefHeight(70);

        buttonList.setAlignment(Pos.CENTER);
        buttonList.getChildren().addAll(setToCurrent,delete,resetVote,kegTapped,newAdmin,removeAdmin);
        adminPopup.setMaxWidth(600);
        adminPopup.setMaxHeight(650);
        adminPopup.setAlignment(Pos.TOP_CENTER);
        adminPopup.getChildren().addAll(header, navPane, buttonList);

        Button thirtyLitre = new Button("30L Keg");
        Button fiftyLitre = new Button("50L Keg");
        Button cancel = new Button("Cancel");
        Button plus = new Button("",displayManager.getImage("plus"));
        Button minus = new Button("",displayManager.getImage("minus"));
        plus.setBackground(Background.EMPTY);
        minus.setBackground(Background.EMPTY);

        thirtyLitre.setOnAction(event -> {
            kegManager.setMaxKegWeight(30);
            kegManager.tareToMaxWeight();
            voteManager.resetVotes();
            dataManager.resetVotes();
            voteManager.setCurrentKeg(voteManager.getCurrentBeer());
            adminPopup.getChildren().remove(newKeggedTapped);
            adminPopup.getChildren().add(buttonList);
        });
        fiftyLitre.setOnAction(event -> {
            kegManager.setMaxKegWeight(50);
            kegManager.tareToMaxWeight();
            voteManager.resetVotes();
            dataManager.resetVotes();
            voteManager.setCurrentKeg(voteManager.getCurrentBeer());
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
        plus.setOnAction(event -> kegManager.adjustTareValue(true));
        minus.setOnAction(event -> kegManager.adjustTareValue(false));

        HBox kegAdjustButtons = new HBox(minus,kegManager.createTareLabel(),plus);
        kegAdjustButtons.setAlignment(Pos.CENTER);
        kegAdjustButtons.setSpacing(30);

        newKeggedTapped.setMaxWidth(600);
        newKeggedTapped.setAlignment(Pos.TOP_CENTER);
        newKeggedTapped.getChildren().addAll(kegSizeButtons,line1,manualAdjust,kegAdjustButtons);

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
}
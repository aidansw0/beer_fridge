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

        BorderPane.setAlignment(root.getLeft(), Pos.CENTER_RIGHT);
        BorderPane.setAlignment(root.getCenter(), Pos.CENTER_LEFT);
        BorderPane.setAlignment(root.getBottom(), Pos.TOP_LEFT);

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
        keyboardFrame = displayManager.createKeyboardLayout(vkb.view());
    }

    private void createAdminPanel() {
        List<Node> elements = new ArrayList<>();
        elements.add(voteManager.createLeftButton());
        elements.add(voteManager.createRightButton());
        elements.add(voteManager.createBeerDisplay());
        elements.add(voteManager.createLikesDisplay());
        elements.add(new Button());
        elements.add(new Button("Reset All Votes"));
        elements.add(new Button("Delete This Beer"));
        elements.add(new Button("Set This Beer to the Current Keg"));
        elements.add(new Button("Tap a New Beer Keg"));
        elements.add(new Button("Add a New Admin"));
        elements.add(new Button("Remove an Admin"));
        elements.add(new Button("30L Keg"));
        elements.add(new Button("50L Keg"));
        elements.add(new Button("Cancel"));
        elements.add(new Button());
        elements.add(new Button());
        elements.add(kegManager.createTareLabel());
        elements.add(new VBox(20));
        elements.add(new VBox(30));
        elements.add(new VBox(15));

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
        VBox newKeggedTapped = (VBox) elements.get(17);
        VBox adminPopup = (VBox) elements.get(18);
        VBox buttonList = (VBox) elements.get(19);

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

        setToCurrent.setOnAction(event -> voteManager.setCurrentKeg(voteManager.getCurrentBeer()));

        delete.setOnAction(event -> {
            System.out.println("Deleting current beer");
            voteManager.deleteCurrentBeer();
        });

        resetVote.setOnAction(event -> {
            System.out.println("Resetting votes");
            voteManager.resetVotes();
            dataManager.resetVotes();
        });

        kegTapped.setOnAction(event -> {
            adminPopup.getChildren().remove(buttonList);
            adminPopup.getChildren().add(newKeggedTapped);
        });

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

        cancel.setOnAction(event -> {
            adminPopup.getChildren().remove(newKeggedTapped);
            adminPopup.getChildren().add(buttonList);
        });

        plus.setOnAction(event -> kegManager.adjustTareValue(true));

        minus.setOnAction(event -> kegManager.adjustTareValue(false));

        adminPanel = displayManager.createAdminPanelLayout(elements);
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

    private void createKegFrame() {
        List<Node> elements = new ArrayList<>();
        elements.add(kegManager.createWeightMeter());
        elements.add(kegManager.createWeightLabel());

        kegFrame = displayManager.createKegLayout(elements);
    }

    private BorderPane createTempFrame() {
        List<Node> elements = new ArrayList<>();
        elements.add(kegManager.createTempLabel());
        elements.add(kegManager.createLineChart());

        return displayManager.createTempLayout(elements);
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


        BorderPane votingFrame = displayManager.createVotingLayout(elements);

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

        ((Label) elements.get(0)).textProperty().bind(voteManager.currentKegProperty());
        ((Button) elements.get(1)).setOnAction(event -> toggleAdminPanel());
        elements.get(1).disableProperty().bind(keyCardListener.adminKeyVerifiedProperty().not());

        // get current keg data from saveData
        Object[] keg = dataManager.readCurrentKeg();
        voteManager.setCurrentKeg((String) keg[0]);
        kegManager.setTare((double) keg[1]);

        footerFrame = displayManager.createFooterLayout(elements);
    }
}
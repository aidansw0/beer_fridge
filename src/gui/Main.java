package gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javafx.scene.Cursor;
import javafx.stage.StageStyle;
import userInputs.KeyCardListener;
import userInputs.VirtualKeyboard;

import org.json.JSONException;

import dataManagement.DweetManager;
import dataManagement.DataManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
    private final StackPane finalStack = new StackPane();
    private Stage window;

    private double initial = 0;
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

        finalStack.getChildren().addAll(displayManager.createRoot());
        displayManager.getRoot().setOnKeyPressed((KeyEvent event) -> keyCardListener.handleEvent(event));

        Scene scene = new Scene(finalStack, 1280, 1024);
        scene.getStylesheets().add("css/linechart.css");
        scene.getStylesheets().add("css/keyboard.css");
        scene.getStylesheets().add("css/main.css");

        scene.setCursor(Cursor.NONE);
        window.initStyle(StageStyle.UNDECORATED);
//        window.setMaxWidth(1280);
//        window.setMaxHeight(1024);

        window.setScene(scene);
        window.show();
    }

    private void toggleAdminPanel() {
        if (adminPanelOn) {
            finalStack.getChildren().remove(displayManager.getAdminPanel());
            keyCardListener.checkAdminKeyVerified(true);
        } else {
            finalStack.getChildren().add(displayManager.getAdminPanel());
        }
        adminPanelOn = !adminPanelOn;
    }

    private void toggleKeyboard(BorderPane votingFrame) {
        if (displayManager.getKeyboardVisibleStatus()) {
            newBeerField.setText("");
            keyCardListener.checkAdminKeyVerified(true);
            votingFrame.setBottom(voteManager.getPollChart());
            displayManager.toggleFooter();
        } else {
            newBeerField.setText("");
            votingFrame.setBottom(newBeerField);
            newBeerField.requestFocus();
            displayManager.toggleFooter();
        }
    }

    private void keyboardEvents(KeyEvent event, BorderPane frame) {
        switch (event.getCode()) {
        case ENTER:
            if (!newBeerField.getText().isEmpty()) {
                if (voteManager.addBeer(newBeerField.getText(), 0)) {
                    toggleKeyboard(frame);
                }
                newBeerField.setText("");
            }
            break;

        case ESCAPE:
            newBeerField.setText("");
            keyCardListener.checkAdminKeyVerified(true);
            frame.setBottom(voteManager.getPollChart());
            toggleKeyboard(frame);
            break;
        }
    }

    private void createKegFrame() {
        List<Node> elements = new ArrayList<>();
        elements.add(kegManager.createWeightMeter());
        elements.add(kegManager.createWeightLabel());

        displayManager.createKegLayout(elements);
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
        ((Button) elements.get(2)).setOnAction(event -> toggleKeyboard(votingFrame));
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
        List<Node> elements = new ArrayList<>();
        elements.add(createTempFrame());
        elements.add(createVotingFrame());

        displayManager.createTempAndVotingLayout(elements);
    }

    private void createFooterFrame() {
        List<Node> elements = new ArrayList<>();
        elements.add(new Label());
        elements.add(new Button());


        // current keg display
        ((Label) elements.get(0)).textProperty().bind(voteManager.currentKegProperty());

        // admin panel button
        ((Button) elements.get(1)).setOnAction(event -> toggleAdminPanel());
        elements.get(1).disableProperty().bind(keyCardListener.adminKeyVerifiedProperty().not());

        // get current keg data from saveData
        Object[] keg = dataManager.readCurrentKeg();
        voteManager.setCurrentKeg((String) keg[0]);
        kegManager.setTare((double) keg[1]);

        displayManager.createFooterLayout(elements);
    }

    private void createKeyboardPopUp() {
        VirtualKeyboard vkb = new VirtualKeyboard(newBeerField);
        displayManager.createKeyboardLayout(vkb.view());
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

        // close button
        ((Button) elements.get(4)).setOnAction(event -> {
            if (((VBox) elements.get(18)).getChildren().contains(elements.get(17))) {
                ((VBox) elements.get(18)).getChildren().remove(elements.get(17));
                ((VBox) elements.get(18)).getChildren().add(elements.get(19));
            }
            ((Button) elements.get(9)).textProperty().unbind();
            ((Button) elements.get(9)).setText("Add a New Admin");
            ((Button) elements.get(10)).textProperty().unbind();
            ((Button) elements.get(10)).setText("Remove an Admin");

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

        // set to current button
        ((Button) elements.get(7)).setOnAction(event -> voteManager.setCurrentKeg(voteManager.getCurrentBeer()));

        // delete button
        ((Button) elements.get(6)).setOnAction(event -> {
            voteManager.deleteCurrentBeer();
        });

        // reset vote button
        ((Button) elements.get(5)).setOnAction(event -> {
            voteManager.resetVotes();
            dataManager.resetVotes();
        });

        // tap a new keg button
        ((Button) elements.get(8)).setOnAction(event -> {
            ((VBox) elements.get(18)).getChildren().remove(elements.get(19));
            ((VBox) elements.get(18)).getChildren().add(elements.get(17));
        });

        // add a new admin button
        ((Button) elements.get(9)).setOnAction(event -> {
            keyCardListener.resetAdminHint();
            ((Button) elements.get(10)).textProperty().unbind();
            ((Button) elements.get(9)).textProperty().bind(keyCardListener.getAdminHintText());
            elements.get(10).setOnKeyPressed(null);
            elements.get(9).setOnKeyPressed((KeyEvent keyEvent) -> {
                if(keyCardListener.handleNewAdmin(keyEvent)) {
                    ((Button) elements.get(9)).textProperty().unbind();
                    elements.get(9).setOnKeyPressed(null);
                }
            });
        });

        // remove an admin button
        ((Button) elements.get(10)).setOnAction(event -> {
            keyCardListener.resetAdminHint();
            ((Button) elements.get(9)).textProperty().unbind();
            ((Button) elements.get(10)).textProperty().bind(keyCardListener.getAdminHintText());
            elements.get(9).setOnKeyPressed(null);
            elements.get(10).setOnKeyPressed((KeyEvent keyEvent) -> {
                if(keyCardListener.handleRemoveAdmin(keyEvent)) {
                    ((Button) elements.get(10)).textProperty().unbind();
                    elements.get(10).setOnKeyPressed(null);
                }
            });
        });

        // tare to thirty litre button
        ((Button) elements.get(11)).setOnAction(event -> {
            kegManager.setMaxKegWeight(30);
            kegManager.tareToMaxWeight();
            voteManager.resetVotes();
            dataManager.resetVotes();
            voteManager.setCurrentKeg(voteManager.getCurrentBeer());
            ((VBox) elements.get(18)).getChildren().remove(elements.get(17));
            ((VBox) elements.get(18)).getChildren().add(elements.get(19));
        });

        // tare to fifty litre button
        ((Button) elements.get(12)).setOnAction(event -> {
            kegManager.setMaxKegWeight(50);
            kegManager.tareToMaxWeight();
            voteManager.resetVotes();
            dataManager.resetVotes();
            voteManager.setCurrentKeg(voteManager.getCurrentBeer());
            ((VBox) elements.get(18)).getChildren().remove(elements.get(17));
            ((VBox) elements.get(18)).getChildren().add(elements.get(19));
        });

        // cancel button, return to main admin menu
        ((Button) elements.get(13)).setOnAction(event -> {
            ((VBox) elements.get(18)).getChildren().remove(elements.get(17));
            ((VBox) elements.get(18)).getChildren().add(elements.get(19));
        });

        // plus button for manually adjusting keg tare value
        ((Button) elements.get(14)).setOnAction(event -> kegManager.adjustTareValue(true));

        // minus button for manually adjusting keg tare value
        ((Button) elements.get(15)).setOnAction(event -> kegManager.adjustTareValue(false));

        displayManager.createAdminPanelLayout(elements);
    }
}
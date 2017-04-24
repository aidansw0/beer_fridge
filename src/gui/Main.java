package gui;
import backend.KegManager;
import backend.KeyCardListener;
import backend.VirtualKeyboard;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.stage.StageStyle;

public class Main extends Application {

    private final KegManager beerKeg = new KegManager();
    private final DataManager dataManager = new DataManager(beerKeg);
    private final VoteManager voteManager = new VoteManager();
    private final KeyCardListener keyCardListener = new KeyCardListener();
    private final Label newBeerField = new Label();
    private Stage window;
    private BorderPane kegFrame, tempAndVotingFrame, footerFrame, keyboardFrame, root;

    private double initial = 0;

    private boolean keyboardOn = false;

    public static void main(String[] args) { launch(args); }

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

        primaryStage.setOnCloseRequest(event -> {
                voteManager.saveToFile();
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
        ImageView keg = importImage("img/keg.png",345);
        ImageView kegheader = importImage("img/kegheader.png",53);

        kegMeter.getStyleClass().add("keg-meter");
        kegVolume.getStyleClass().add("data-labels");

        kegMeterStack.getChildren().add(kegMeter);
        kegMeterStack.getChildren().add(kegVolume);

        StackPane.setAlignment(kegVolume, Pos.BOTTOM_LEFT);
        StackPane.setMargin(kegVolume, new Insets(0,0,20,60));
        StackPane.setAlignment(kegMeter, Pos.BOTTOM_LEFT);
        StackPane.setMargin(kegMeter, new Insets(0,0,43,5));

        kegFrame.getStyleClass().addAll("all-frames","keg-frame");
        kegFrame.setPrefSize(445,450);
        kegFrame.setTop(kegheader);
        kegFrame.setLeft(keg);
        kegFrame.setCenter(kegMeterStack);

        BorderPane.setAlignment(keg, Pos.BOTTOM_LEFT);
        BorderPane.setMargin(keg, new Insets(15,10,15,30));
    }

    private BorderPane createTempFrame() {
        BorderPane tempFrame = new BorderPane();
        Label temperature = dataManager.createTempLabel();
        ImageView tempheader = importImage("img/tempheader.png",53);

        temperature.getStyleClass().add("data-labels");

        tempFrame.getStyleClass().addAll("all-frames","temp-frame");
        tempFrame.setPrefSize(715,270);
        tempFrame.setMaxWidth(715);
        tempFrame.setTop(tempheader);
        tempFrame.setCenter(dataManager.createLineChart());
        tempFrame.setRight(temperature);

        BorderPane.setAlignment(temperature, Pos.TOP_RIGHT);
        BorderPane.setMargin(temperature, new Insets(5,30,0,0));

        return tempFrame;
    }

    private BorderPane createVotingFrame() {
        StackPane votingHeader = new StackPane();
        BorderPane votingFrame = new BorderPane();
        BorderPane navPane = new BorderPane();
        VBox likePane = new VBox();
        Text beerDisplay = new Text(voteManager.getCurrentBeer());
        Text votes = new Text(voteManager.getCurrentVotes() + " Votes");
        Text pressToVote = new Text("Press to Vote");
        Text pleaseScanCard = new Text ("Please scan card ...");
        Button addButton = new Button();

        // Import images
        ImageView votingheaderimg = importImage("img/votingheader.png",53);
        ImageView thumb = importImage("img/like.png",60);
        ImageView navleft = importImage("img/navleft.png",60);
        ImageView navright = importImage("img/navright.png",60);
        ImageView plusimg = importImage("img/add.png",30);

        newBeerField.getStyleClass().add("new-beer-field");
        newBeerField.setPrefWidth(715);
        newBeerField.setOnKeyPressed((KeyEvent event) -> keyboardEvents(event,votingFrame));
        newBeerField.disableProperty().bind(keyCardListener.adminKeyVerifiedProperty().not());

        addButton.setGraphic(plusimg);
        addButton.setBackground(Background.EMPTY);
        addButton.disableProperty().bind(keyCardListener.adminKeyVerifiedProperty().not());
        addButton.setOnAction(event -> {
            if (keyboardOn) {
                keyCardListener.checkAdminKeyVerified(true);
                votingFrame.setBottom(voteManager.getPollChart());
                toggleKeyboard();
            }
            else {
                newBeerField.setText("");
                votingFrame.setBottom(newBeerField);
                newBeerField.requestFocus();
                toggleKeyboard();
            }
        });

        votingHeader.getChildren().add(votingheaderimg);
        votingHeader.getChildren().add(addButton);
        votingHeader.getChildren().add(pleaseScanCard);
        StackPane.setAlignment(addButton, Pos.CENTER_RIGHT);
        StackPane.setMargin(addButton, new Insets(0,15,0,0));

        Button left = voteManager.createLeftButton(beerDisplay,navleft);
        Button right = voteManager.createRightButton(beerDisplay,navright);
        Button like = voteManager.createLikeButton(votes,thumb,keyCardListener);
        like.disableProperty().bind(keyCardListener.regularKeyVerifiedProperty().not());

        beerDisplay.getStyleClass().add("beer-display");
        votes.getStyleClass().add("votes-display");
        pressToVote.getStyleClass().add("press-to-vote");
        pleaseScanCard.getStyleClass().add("votes-display");
        pleaseScanCard.textProperty().bind(keyCardListener.getHintText());

        votingFrame.getStyleClass().addAll("all-frames","voting-frame");
        votingFrame.setPrefSize(715,150);
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
                }
                else if (event.getX() > initial + 100){
                    voteManager.swipeRight();
                }
            }
        });

        navPane.setLeft(left);
        navPane.setRight(right);
        navPane.setCenter(beerDisplay);
        navPane.setPrefSize(490,90);

        likePane.getChildren().add(pressToVote);
        likePane.getChildren().add(votes);
        likePane.alignmentProperty().set(Pos.CENTER);

        votingFrame.setTop(votingHeader);
        votingFrame.setRight(navPane);
        votingFrame.setCenter(like);
        votingFrame.setLeft(likePane);
        votingFrame.setBottom(voteManager.createPollChart());

        BorderPane.setAlignment(left, Pos.CENTER_LEFT);
        BorderPane.setAlignment(right, Pos.CENTER_RIGHT);
        BorderPane.setAlignment(like, Pos.CENTER_LEFT);
        BorderPane.setMargin(likePane, new Insets(0,5,0,20));
        BorderPane.setMargin(navPane, new Insets(5,10,5,10));
        BorderPane.setMargin(votingFrame.getBottom(), new Insets(5,5,20,5));

        return votingFrame;
    }

    private void createTempAndVotingFrame() {
        BorderPane tempFrame = createTempFrame();
        BorderPane votingFrame = createVotingFrame();

        tempAndVotingFrame = new BorderPane();
        tempAndVotingFrame.getStyleClass().add("temp-voting-frame");
        tempAndVotingFrame.setPrefSize(715,450);
        tempAndVotingFrame.setTop(tempFrame);
        tempAndVotingFrame.setCenter(votingFrame);

        BorderPane.setAlignment(votingFrame,Pos.TOP_LEFT);
        BorderPane.setAlignment(tempFrame,Pos.TOP_LEFT);
    }

    private void createFooterFrame() {
        footerFrame = new BorderPane();
        Label currentKeg = new Label("Steamworks Kolsch");

        // Import images
        ImageView footerheader = importImage("img/footerheader.png",53);
        ImageView teralogo = importImage("img/teralogo.png",146);

        currentKeg.getStyleClass().add("data-labels");

        footerFrame.getStyleClass().addAll("all-frames","footer-frame");
        footerFrame.setPrefWidth(1190);
        footerFrame.setMaxWidth(1190);
        footerFrame.setTop(footerheader);
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

        root = new BorderPane();
        root.setPrefSize(1190,450);
        root.getStyleClass().add("main-window");
        root.setLeft(kegFrame);
        root.setCenter(tempAndVotingFrame);
        root.setBottom(footerFrame);
        root.setOnKeyPressed((KeyEvent event) -> keyCardListener.handleEvent(event));

        BorderPane.setAlignment(kegFrame, Pos.CENTER_RIGHT);
        BorderPane.setAlignment(tempAndVotingFrame, Pos.CENTER_LEFT);
        BorderPane.setAlignment(footerFrame, Pos.TOP_LEFT);

        Scene scene = new Scene(root, 1280, 1024);
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

    private void createKeyboardPopUp () {
        VirtualKeyboard vkb = new VirtualKeyboard(newBeerField);
        Node keys = vkb.view();

        keyboardFrame = new BorderPane();
        keyboardFrame.setCenter(keys);
        keyboardFrame.setPrefWidth(1190);
        keyboardFrame.setMaxWidth(1190);

        BorderPane.setAlignment(keyboardFrame, Pos.TOP_LEFT);
        BorderPane.setMargin(keyboardFrame, new Insets(15,50,45,50));
    }

    private void toggleKeyboard() {
        if (keyboardOn) {
            root.setBottom(footerFrame);
            keyboardOn = !keyboardOn;
        }
        else {
            root.setBottom(keyboardFrame);
            keyboardOn = !keyboardOn;
        }
    }

    private void keyboardEvents(KeyEvent event, BorderPane frame) {
        switch (event.getCode()) {
            case ENTER:
                if (!newBeerField.getText().isEmpty()) {
                    if (keyCardListener.checkRegularKeyVerified(false)) {
                        if (voteManager.addBeer(newBeerField.getText(), 0)) {
                            keyCardListener.checkRegularKeyVerified(true);
                            frame.setBottom(voteManager.getPollChart());
                            toggleKeyboard();
                        }
                        newBeerField.setText("");
                    }
                    // Access has expired
                    else {
                        frame.setBottom(voteManager.getPollChart());
                        toggleKeyboard();
                        newBeerField.setText("");
                    }
                }
                break;

            case ESCAPE:
                newBeerField.setText("");
                keyCardListener.checkRegularKeyVerified(true);
                frame.setBottom(voteManager.getPollChart());
                toggleKeyboard();
                break;
        }
    }

    private ImageView importImage (String imgPath, int fitHeight) {
        ImageView imgView = new ImageView(imgPath);
        imgView.setFitHeight(fitHeight);
        imgView.setPreserveRatio(true);

        return imgView;
    }
}
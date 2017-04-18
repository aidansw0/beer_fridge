package gui;
import backend.KegManager;
import backend.VirtualKeyboard;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class Main extends Application {

    private DataManager dataManager;
    private VoteManager buttons;
    private Stage window;
    private Font latoHairline;
    private BorderPane kegFrame, tempAndVotingFrame, footerFrame, keyboardFrame, root;

    private boolean keyboardOn = false;

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) throws Exception {
        init(primaryStage);
        createScene();
    }

    private void init(Stage primaryStage) {
        KegManager beerKeg = new KegManager();
        buttons = new VoteManager();
        dataManager = new DataManager(beerKeg);

        window = primaryStage;
        window.setTitle("Beer Keg Monitor");
        window.isFullScreen();
        window.initStyle(StageStyle.UNDECORATED);

        latoHairline = Font.loadFont(getClass()
                .getResourceAsStream("/css/Lato-Hairline.ttf"), 80);

        Font.loadFont(getClass()
                .getResourceAsStream("/css/Lato-Light.ttf"), 20);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.exit();
                System.exit(0);
            }
        });
    }

    private void createKegFrame() {
        kegFrame = new BorderPane();
        StackPane kegMeterStack = new StackPane();
        Polygon kegMeter = dataManager.createWeightMeter();
        Label kegVolume = dataManager.createWeightLabel();

        // Import images
        Image img1 = new Image("img/keg.png");
        Image img2 = new Image("img/kegheader.png");

        ImageView keg = new ImageView();
        keg.setImage(img1);
        keg.setFitHeight(345);
        keg.setPreserveRatio(true);

        ImageView kegheader = new ImageView();
        kegheader.setImage(img2);
        kegheader.setFitHeight(53);
        kegheader.setPreserveRatio(true);

        kegMeter.setFill(Color.web("06d3ce"));
        kegVolume.setTextFill(Color.web("cacaca"));
        kegVolume.setFont(latoHairline);

        kegMeterStack.getChildren().add(kegMeter);
        kegMeterStack.getChildren().add(kegVolume);

        StackPane.setAlignment(kegVolume, Pos.BOTTOM_LEFT);
        StackPane.setMargin(kegVolume, new Insets(0,0,20,60));
        StackPane.setAlignment(kegMeter, Pos.BOTTOM_LEFT);
        StackPane.setMargin(kegMeter, new Insets(0,0,43,5));

        kegFrame.setPrefSize(445,450);
        kegFrame.setStyle("-fx-background-color: #2d2d2d");
        kegFrame.setTop(kegheader);
        kegFrame.setLeft(keg);
        kegFrame.setCenter(kegMeterStack);

        BorderPane.setAlignment(keg, Pos.BOTTOM_LEFT);
        BorderPane.setMargin(keg, new Insets(15,10,15,30));
    }

    private BorderPane createTempFrame() {
        BorderPane tempFrame = new BorderPane();
        Label temperature = dataManager.createTempLabel();
        Image img3 = new Image("img/tempheader.png");

        ImageView tempheader = new ImageView();
        tempheader.setImage(img3);
        tempheader.setFitHeight(53);
        tempheader.setPreserveRatio(true);

        temperature.setTextFill(Color.web("cacaca"));
        temperature.setFont(latoHairline);

        tempFrame.setPrefSize(715,270);
        tempFrame.setMaxWidth(715);
        tempFrame.setStyle("-fx-background-color: #2d2d2d");
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
        Text beerDisplay = new Text(buttons.getCurrentBeer());
        Text votes = new Text(buttons.getCurrentVotes() + " Votes");
        Text pressToVote = new Text("Press to Vote");
        TextField addBeer = new TextField();
        Button addButton = new Button();

        Image img1 = new Image("img/votingheader.png");
        Image img2 = new Image("img/navleft.png");
        Image img3 = new Image("img/navright.png");
        Image img4 = new Image("img/like.png");
        Image img5 = new Image("img/add.png");

        addBeer.getStyleClass().add("new-beer-field");
        addBeer.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                buttons.addBeer(addBeer.getText(), 0);
                addBeer.clear();
                votingFrame.setBottom(buttons.getPollChart());
                toggleKeyboard();
            }
        });

        ImageView plusimg = new ImageView();
        plusimg.setImage(img5);
        plusimg.setFitHeight(30);
        plusimg.setPreserveRatio(true);
        addButton.setGraphic(plusimg);
        addButton.setBackground(Background.EMPTY);
        addButton.setOnAction(event -> {
            if (keyboardOn) {
                votingFrame.setBottom(buttons.getPollChart());
            }
            else {
                votingFrame.setBottom(addBeer);
                addBeer.requestFocus();
            }
            toggleKeyboard();
        });

        ImageView votingheaderimg = new ImageView();
        votingheaderimg.setImage(img1);
        votingheaderimg.setFitHeight(53);
        votingheaderimg.setPreserveRatio(true);

        votingHeader.getChildren().add(votingheaderimg);
        votingHeader.getChildren().add(addButton);
        StackPane.setAlignment(addButton, Pos.CENTER_RIGHT);
        StackPane.setMargin(addButton, new Insets(0,15,0,0));

        ImageView navleft = new ImageView();
        navleft.setImage(img2);
        navleft.setFitHeight(60);
        navleft.setPreserveRatio(true);

        ImageView navright = new ImageView();
        navright.setImage(img3);
        navright.setFitHeight(60);
        navright.setPreserveRatio(true);

        ImageView thumb = new ImageView();
        thumb.setImage(img4);
        thumb.setFitHeight(60);
        thumb.setPreserveRatio(true);

        beerDisplay.setStyle("-fx-font-family:'Lato Light'; -fx-font-size:40");
        beerDisplay.setFill(Color.web("cacaca"));
        votes.setStyle("-fx-font-family:'Lato Light'; -fx-font-size:20");
        votes.setFill(Color.web("cacaca"));
        pressToVote.setStyle("-fx-font-family:'Lato Light'; -fx-font-size:15");
        pressToVote.setFill(Color.web("cacaca"));

        Button left = buttons.createLeftButton(beerDisplay,navleft);
        Button right = buttons.createRightButton(beerDisplay,navright);
        Button like = buttons.createLikeButton(votes, thumb);

        votingFrame.setPrefSize(715,150);
        votingFrame.setMaxWidth(715);
        votingFrame.setStyle("-fx-background-color: #2d2d2d");

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
        votingFrame.setBottom(buttons.createPollChart());

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
        tempAndVotingFrame.setPrefSize(715,450);
        tempAndVotingFrame.setTop(tempFrame);
        tempAndVotingFrame.setCenter(votingFrame);

        BorderPane.setAlignment(tempFrame, Pos.TOP_LEFT);
        BorderPane.setMargin(tempFrame, new Insets(0,0,15,0));
        BorderPane.setAlignment(votingFrame, Pos.TOP_LEFT);
        BorderPane.setMargin(votingFrame, new Insets(15,0,0,0));
    }

    private void createFooterFrame() {
        footerFrame = new BorderPane();
        Label currentKeg = new Label("Steamworks IPA");
        Image img5 = new Image("img/footerheader.png");
        Image img6 = new Image("img/teralogo.png");

        ImageView footerheader = new ImageView();
        footerheader.setImage(img5);
        footerheader.setFitHeight(53);
        footerheader.setPreserveRatio(true);

        ImageView teralogo = new ImageView();
        teralogo.setImage(img6);
        teralogo.setFitHeight(146);
        teralogo.setPreserveRatio(true);

        currentKeg.setTextFill(Color.web("cacaca"));
        currentKeg.setFont(latoHairline);

        footerFrame.setPrefWidth(1190);
        footerFrame.setMaxWidth(1190);
        footerFrame.setStyle("-fx-background-color: #2d2d2d");
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
        root.setStyle("-fx-background-color: #1e1e1e");
        root.setLeft(kegFrame);
        root.setCenter(tempAndVotingFrame);
        root.setBottom(footerFrame);

        BorderPane.setAlignment(kegFrame, Pos.CENTER_RIGHT);
        BorderPane.setMargin(kegFrame, new Insets(45,15,15,50));
        BorderPane.setAlignment(tempAndVotingFrame, Pos.CENTER_LEFT);
        BorderPane.setMargin(tempAndVotingFrame, new Insets(45,50,15,15));
        BorderPane.setAlignment(footerFrame, Pos.TOP_LEFT);
        BorderPane.setMargin(footerFrame, new Insets(15,50,45,50));

        Scene scene = new Scene(root, 1280, 1024);
        scene.getStylesheets().add("css/linechart.css");
        scene.getStylesheets().add("css/keyboard.css");
        scene.setCursor(Cursor.NONE);
        window.setScene(scene);
        window.show();
    }

    private void createKeyboardPopUp () {
        VirtualKeyboard vkb = new VirtualKeyboard();
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
}
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.control.Label;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Stage window;
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        window = primaryStage;
        window.setTitle("Beer Keg Monitor");

        //Keg Frame
        Image img1 = new Image("img/keg.png");
        ImageView keg = new ImageView();
        keg.setImage(img1);
        keg.setFitHeight(345);
        keg.setPreserveRatio(true);

        Image img2 = new Image("img/kegheader.png");
        ImageView kegheader = new ImageView();
        kegheader.setImage(img2);
        kegheader.setFitWidth(445);
        kegheader.setPreserveRatio(true);

        Polygon kegMeter = new Polygon();
        kegMeter.setFill(Color.web("06d3ce"));
        kegMeter.getPoints().addAll(4.7, 0.0,
                                    71.8, 0.0,
                                    30.6, 232.1,
                                    0.0, 232.3);

        Label kegVolume = new Label ("33L");
        kegVolume.setTextFill(Color.web("ffffff"));
        kegVolume.setFont(new Font("Helvetica Neue UltraLight", 80));

        StackPane kegMeterStack = new StackPane();
        kegMeterStack.getChildren().add(kegMeter);
        kegMeterStack.getChildren().add(kegVolume);

        StackPane.setAlignment(kegVolume, Pos.BOTTOM_LEFT);
        StackPane.setMargin(kegVolume, new Insets(0,0,40,60));
        StackPane.setAlignment(kegMeter, Pos.BOTTOM_LEFT);
        StackPane.setMargin(kegMeter, new Insets(50,0,58,5));

        BorderPane kegFrame = new BorderPane();
        kegFrame.setPrefSize(445,450);
        kegFrame.setStyle("-fx-background-color: #2d2d2d");
        kegFrame.setTop(kegheader);
        kegFrame.setLeft(keg);
        kegFrame.setCenter(kegMeterStack);

        BorderPane.setAlignment(keg, Pos.CENTER_LEFT);
        BorderPane.setMargin(keg, new Insets(0,5,0,30));

        // FlowPane
        FlowPane root = new FlowPane();
        root.getChildren().add(kegFrame);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 445, 450);
        window.setScene(scene);
        window.show();
    }
}
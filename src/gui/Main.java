package gui;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Stage window;
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        //BeerKeg beerKeg = new BeerKeg();
        KegManager beerKeg = new KegManager();

        window = primaryStage;
        window.setTitle("Beer Keg Monitor");

        Rectangle coffee = new Rectangle(600,600,Color.rgb(76,65,61));
        Rectangle temp = new Rectangle(178,250,Color.rgb(192,38,38));

        Image image1 = new Image("img/coffeepot.png");
        ImageView coffeepot = new ImageView();
        coffeepot.setImage(image1);
        coffeepot.setFitHeight(bounds.getHeight());
        coffeepot.setPreserveRatio(true);

        Image image2 = new Image("img/thermo.png");
        ImageView thermo = new ImageView();
        thermo.setImage(image2);
        thermo.setFitHeight(bounds.getHeight());
        thermo.setPreserveRatio(true);

        StackPane LeftCoffee = new StackPane();
        LeftCoffee.setAlignment(Pos.BOTTOM_CENTER);
        LeftCoffee.getChildren().add(coffee);
        LeftCoffee.getChildren().add(coffeepot);

        StackPane RightTemp = new StackPane();
        RightTemp.setAlignment(Pos.BOTTOM_CENTER);
        RightTemp.getChildren().add(temp);
        RightTemp.getChildren().add(thermo);

        FlowPane root = new FlowPane();
        root.getChildren().add(LeftCoffee);
        root.getChildren().add(RightTemp);
        root.setAlignment(Pos.CENTER);

        coffeepot.fitHeightProperty().bind(root.heightProperty());
        coffee.heightProperty().bind(beerKeg.weightProperty().multiply(root.heightProperty()).divide(100));
        coffee.widthProperty().bind(root.heightProperty().multiply(0.87));

        thermo.fitHeightProperty().bind(root.heightProperty());
        temp.heightProperty().bind(beerKeg.tempProperty().multiply(root.heightProperty()).divide(100));
        temp.widthProperty().bind(root.heightProperty().multiply(0.28));

        Scene scene = new Scene(root, 400, 300);
        window.setScene(scene);
        window.show();
    }
}
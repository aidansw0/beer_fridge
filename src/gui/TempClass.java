package gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import backend.SaveData;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class TempClass extends Application {
    
    int currentBeer = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Stage window;

        window = primaryStage;
        window.setTitle("Beer Keg Monitor");
        
        

        Map<String, Integer> beerTypeLikes = new HashMap<String, Integer>();
        beerTypeLikes.put("beer0", 0);
        beerTypeLikes.put("beer1", 0);
        beerTypeLikes.put("beer2", 0);
        beerTypeLikes.put("beer3", 0);
        
        List<String> beerTypes = new ArrayList<String>();
        beerTypes.add("beer0");
        beerTypes.add("beer1");
        beerTypes.add("beer2");
        beerTypes.add("beer3");
        
        SaveData test = new SaveData(beerTypeLikes, beerTypes);
        
        Text displayBeer = new Text(beerTypes.get(currentBeer));
        displayBeer.setVisible(true);
        
        TextField enterNewBeer = new TextField();

        Button like = new Button("Like");
        like.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                //event.consume();
                String beerToUpvote = beerTypes.get(currentBeer);
                beerTypeLikes.put(beerToUpvote, beerTypeLikes.get(beerToUpvote) + 1);
                System.out.println(beerTypeLikes.get(beerToUpvote));
            }
        });

        Button left = new Button("Left");
        left.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (currentBeer > 0) {
                    currentBeer--;
                } else if (currentBeer == 0) {
                    currentBeer = beerTypes.size() - 1;
                }
                
                displayBeer.setText(beerTypes.get(currentBeer));
            }
        });

        Button right = new Button("Right");
        right.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                if (currentBeer < beerTypes.size() - 1) {
                    currentBeer++;
                } else if (currentBeer == beerTypes.size() - 1) {
                    currentBeer = 0;
                }
                
                displayBeer.setText(beerTypes.get(currentBeer));
            }
        });

        Button enter = new Button("Enter");
        enter.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                String beerToAdd = enterNewBeer.getText();
                enterNewBeer.clear();
                
                if (!beerTypeLikes.containsKey(beerToAdd)) {
                    beerTypeLikes.put(beerToAdd, 0);
                    beerTypes.add(beerToAdd);
                } else {
                    System.out.println("This beer is already in the database!");
                }
            }
        });
        

        BorderPane leftPane = new BorderPane();
        BorderPane.setAlignment(left, Pos.TOP_LEFT);
        leftPane.setCenter(left);
        leftPane.setVisible(true);
        
        BorderPane rightPane = new BorderPane();
        BorderPane.setAlignment(right, Pos.TOP_RIGHT);
        rightPane.setCenter(right);
        rightPane.setVisible(true);
        
        BorderPane likePane = new BorderPane();
        BorderPane.setAlignment(like, Pos.BOTTOM_LEFT);
        likePane.setCenter(like);
        likePane.setVisible(true);
        
        BorderPane textPane = new BorderPane();
        BorderPane.setAlignment(enterNewBeer, Pos.CENTER);
        textPane.setCenter(enterNewBeer);
        textPane.setVisible(true);
        
        BorderPane enterPane = new BorderPane();
        BorderPane.setAlignment(enter, Pos.BOTTOM_RIGHT);
        enterPane.setCenter(enter);
        enterPane.setVisible(true);
        
        BorderPane displayPane =  new BorderPane();
        BorderPane.setAlignment(displayBeer, Pos.TOP_CENTER);
        displayPane.setCenter(displayBeer);
        displayPane.setVisible(true);
       
        FlowPane root = new FlowPane();
        root.getChildren().add(leftPane);
        root.getChildren().add(rightPane);
        root.getChildren().add(likePane);
        root.getChildren().add(textPane);
        root.getChildren().add(enterPane);
        root.getChildren().add(displayPane);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 400, 300);
        window.setScene(scene);
        window.show();
    }
}

package gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ButtonManager {
    
    private final Map<String, Integer> beerTypeLikes = new HashMap<String, Integer>();
    private final List<String> beerTypes = new ArrayList<String>();
    private Text likesDisplay;
    private int currentBeer = 0;

    public ButtonManager() {
        addBeer("Stanley Park Brewery");
        addBeer("Blue Buck");
        addBeer("Red Truck");
        addBeer("Post Mark");
    }

    public String getCurrentBeer() { return beerTypes.get(currentBeer); }

    public String getCurrentVotes() { return beerTypeLikes.get(beerTypes.get(currentBeer)).toString(); }

    public void addBeer(String newBeer) {
        beerTypeLikes.put(newBeer, 0);
        beerTypes.add(newBeer);
    }

    public Button createLeftButton(Text display, ImageView img) {
        Button left = new Button("", img);
        left.setBackground(Background.EMPTY);
        left.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (currentBeer > 0) {
                    currentBeer--;
                } else if (currentBeer == 0) {
                    currentBeer = beerTypes.size() - 1;
                }

                display.setText(beerTypes.get(currentBeer));
                likesDisplay.setText(beerTypeLikes.get(beerTypes.get(currentBeer)).toString() + " Votes");
            }
        });
        return left;
    }

    public Button createRightButton(Text display, ImageView img) {
        Button right = new Button("", img);
        right.setBackground(Background.EMPTY);
        right.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                if (currentBeer < beerTypes.size() - 1) {
                    currentBeer++;
                } else if (currentBeer == beerTypes.size() - 1) {
                    currentBeer = 0;
                }

                display.setText(beerTypes.get(currentBeer));
                likesDisplay.setText(beerTypeLikes.get(beerTypes.get(currentBeer)).toString() + " Votes");
            }
        });
        return right;
    }

    public Button createLikeButton(ImageView img, Text likes) {
        Button like = new Button("", img);
        likesDisplay = likes;
        like.setBackground(Background.EMPTY);
        like.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                String beerToUpvote = beerTypes.get(currentBeer);
                beerTypeLikes.put(beerToUpvote, beerTypeLikes.get(beerToUpvote) + 1);
                likesDisplay.setText(beerTypeLikes.get(beerTypes.get(currentBeer)).toString() + " Votes");
                System.out.println(beerTypeLikes.get(beerToUpvote));
            }
        });
        return like;
    }
}

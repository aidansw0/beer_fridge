package gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ButtonManager {
    
    private final Map<String, Integer> beerTypeLikes = new HashMap<String, Integer>();
    private final List<String> beerTypes = new ArrayList<String>();
    private final Map<String,Rectangle> beerVotesBar = new HashMap<String,Rectangle>();
    private final HBox pollsPane = new HBox();

    private final int MAX_BAR_HEIGHT = 100;
    private final Color unselectedColour = Color.web("006B68");
    private final Color selectedColour = Color.web("06D3CE");

    private Text display, likesDisplay;
    private int currentBeer = 0;
    private int highestVote = 0;

    public ButtonManager() {
        addBeer("1. Stanley Park Brewery", 5);
        addBeer("2. Blue Buck",10);
        addBeer("3. Red Truck",12);
        addBeer("4. Post Mark",3);
        addBeer("5. Guinness", 5);
        addBeer("6. Yellow Dog",10);
        addBeer("7. Kokanee",12);
        addBeer("8. Bud Light",3);
        addBeer("9. Corona",12);
        addBeer("10. Stella Artois",3);
    }

    public String getCurrentBeer() { return beerTypes.get(currentBeer); }

    public String getCurrentVotes() { return beerTypeLikes.get(beerTypes.get(currentBeer)).toString(); }

    public void addBeer(String newBeer, int votes) {
        beerTypeLikes.put(newBeer,votes);
        beerTypes.add(newBeer);
    }

    public Button createLeftButton(Text textObject, ImageView img) {
        display = textObject;
        Button left = new Button("", img);
        left.setBackground(Background.EMPTY);
        left.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (currentBeer > 0) {
                    goToElement(currentBeer - 1);
                } else if (currentBeer == 0) {
                    goToElement(beerTypes.size() - 1);
                }
            }
        });
        return left;
    }

    public Button createRightButton(Text textObject, ImageView img) {
        display = textObject;
        Button right = new Button("", img);
        right.setBackground(Background.EMPTY);
        right.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                if (currentBeer < beerTypes.size() - 1) {
                    goToElement((currentBeer + 1));
                } else if (currentBeer == beerTypes.size() - 1) {
                    goToElement(0);
                }
            }
        });
        return right;
    }

    public Button createLikeButton(ImageView img, Text likes) {
        Button likeButton = new Button("", img);
        likesDisplay = likes;
        likeButton.setBackground(Background.EMPTY);

        for (int i=0; i<beerTypeLikes.size(); i++) {
            if (beerTypeLikes.get(beerTypes.get(i)) > highestVote) {
                highestVote = beerTypeLikes.get(beerTypes.get(i));
            }
        }

        likeButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                String beerToUpvote = beerTypes.get(currentBeer);
                beerTypeLikes.put(beerToUpvote, beerTypeLikes.get(beerToUpvote) + 1);
                likesDisplay.setText(beerTypeLikes.get(beerTypes.get(currentBeer)).toString() + " Votes");

                if (beerTypeLikes.get(beerToUpvote) > highestVote) {
                    highestVote = beerTypeLikes.get(beerToUpvote);
                    updatePollChart(beerToUpvote,true);
                }
                else {
                    updatePollChart(beerToUpvote,false);
                }
            }
        });
        return likeButton;
    }

    public HBox createPollChart(){
        final int fixedWeight = 55;

        final int maxBeersDisplayed = 10;
        int beersToDisplay = beerTypes.size();

        if (beersToDisplay > maxBeersDisplayed) {
            beersToDisplay = maxBeersDisplayed;
        }

        for (int i=0; i<beersToDisplay; i++) {
            int rectHeight = beerTypeLikes.get(beerTypes.get(i)) * MAX_BAR_HEIGHT / highestVote;
            String current = beerTypes.get(i);
            beerVotesBar.put(current, new Rectangle(fixedWeight,rectHeight));
            beerVotesBar.get(current).setFill(unselectedColour);

            // Update current beer and highlight
            beerVotesBar.get(current).setOnMouseClicked(event -> goToElement(current));

            // Add all chart elements to pane
            pollsPane.getChildren().add(beerVotesBar.get(beerTypes.get(i)));
        }

        beerVotesBar.get(beerTypes.get(currentBeer)).setFill(selectedColour);

        pollsPane.setSpacing(10);
        pollsPane.setAlignment(Pos.CENTER);
        return pollsPane;
    }

    private void goToElement(String newIndex) {
        System.out.println(newIndex);
        beerVotesBar.get(beerTypes.get(currentBeer)).setFill(unselectedColour);
        beerVotesBar.get(newIndex).setFill(selectedColour);
        currentBeer = beerTypes.indexOf(newIndex);

        display.setText(beerTypes.get(currentBeer));
        likesDisplay.setText(beerTypeLikes.get(beerTypes.get(currentBeer)).toString() + " Votes");
    }

    private void goToElement(int newIndex) {
        System.out.println(newIndex);
        beerVotesBar.get(beerTypes.get(currentBeer)).setFill(unselectedColour);
        beerVotesBar.get(beerTypes.get(newIndex)).setFill(selectedColour);
        currentBeer = newIndex;

        display.setText(beerTypes.get(currentBeer));
        likesDisplay.setText(beerTypeLikes.get(beerTypes.get(currentBeer)).toString() + " Votes");
    }

    private void updatePollChart(String beerToUpdate, boolean updateAll) {
        final int maxBeersDisplayed = 10;
        int beersToDisplay = beerTypes.size();

        if (beersToDisplay > maxBeersDisplayed) {
            beersToDisplay = maxBeersDisplayed;
        }

        if (updateAll) {
            for (int i=0; i<beersToDisplay; i++) {
                String updateBeer = beerTypes.get(i);
                int newHeight = beerTypeLikes.get(updateBeer) * MAX_BAR_HEIGHT / highestVote;
                beerVotesBar.get(updateBeer).setHeight(newHeight);
            }
        }
        else {
            int newHeight = beerTypeLikes.get(beerToUpdate) * MAX_BAR_HEIGHT / highestVote;
            beerVotesBar.get(beerToUpdate).setHeight(newHeight);
        }
    }
}
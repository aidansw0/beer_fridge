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
    private final int maxBarHeight = 100;
    private Text likesDisplay;
    private int currentBeer = 0;
    private int highestVote = 0;

    public ButtonManager() {
        addBeer("1. Stanley Park Brewery", 5);
        addBeer("2. Blue Buck",10);
        addBeer("3. Red Truck",12);
        addBeer("4. Post Mark",3);
        addBeer("5. Stanley Park Brewery", 5);
        addBeer("6. Blue Buck",10);
        addBeer("7. Red Truck",12);
        addBeer("8. Post Mark",3);
        addBeer("9. Red Truck",12);
        addBeer("10. Post Mark",3);

    }

    public String getCurrentBeer() { return beerTypes.get(currentBeer); }

    public String getCurrentVotes() { return beerTypeLikes.get(beerTypes.get(currentBeer)).toString(); }

    public void addBeer(String newBeer, int votes) {
        beerTypeLikes.put(newBeer, votes);
        beerTypes.add(newBeer);
    }

    public Button createLeftButton(Text display, ImageView img) {
        Button left = new Button("", img);
        left.setBackground(Background.EMPTY);
        left.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                beerVotesBar.get(beerTypes.get(currentBeer)).setFill(Color.web("06D3CE"));

                if (currentBeer > 0) {
                    currentBeer--;
                } else if (currentBeer == 0) {
                    currentBeer = beerTypes.size() - 1;
                }

                beerVotesBar.get(beerTypes.get(currentBeer)).setFill(Color.web("008A87"));
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
                beerVotesBar.get(beerTypes.get(currentBeer)).setFill(Color.web("06D3CE"));

                if (currentBeer < beerTypes.size() - 1) {
                    currentBeer++;
                } else if (currentBeer == beerTypes.size() - 1) {
                    currentBeer = 0;
                }

                beerVotesBar.get(beerTypes.get(currentBeer)).setFill(Color.web("008A87"));
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

        for (int i=0; i<beerTypeLikes.size(); i++) {
            if (beerTypeLikes.get(beerTypes.get(i)) > highestVote) {
                highestVote = beerTypeLikes.get(beerTypes.get(i));
            }
        }

        like.setOnAction(new EventHandler<ActionEvent>() {

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
        return like;
    }

    public HBox createPollChart(){
        final int fixedWeight = 55;

        final int maxBeersDisplayed = 10;
        int beersToDisplay = beerTypes.size();

        if (beersToDisplay > maxBeersDisplayed) {
            beersToDisplay = maxBeersDisplayed;
        }

        for (int i=0; i<beersToDisplay; i++) {
            int rectHeight = beerTypeLikes.get(beerTypes.get(i)) * maxBarHeight / highestVote;
            beerVotesBar.put(beerTypes.get(i), new Rectangle(fixedWeight,rectHeight));
            beerVotesBar.get(beerTypes.get(i)).setFill(Color.web("06D3CE"));
            pollsPane.getChildren().add(beerVotesBar.get(beerTypes.get(i)));
        }

        beerVotesBar.get(beerTypes.get(currentBeer)).setFill(Color.web("008A87"));

        pollsPane.setSpacing(10);
        pollsPane.setAlignment(Pos.CENTER);
        return pollsPane;
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
                int newHeight = beerTypeLikes.get(updateBeer) * maxBarHeight / highestVote;
                beerVotesBar.get(updateBeer).setHeight(newHeight);
            }
        }
        else {
            int newHeight = beerTypeLikes.get(beerToUpdate) * maxBarHeight / highestVote;
            beerVotesBar.get(beerToUpdate).setHeight(newHeight);
        }
    }
}
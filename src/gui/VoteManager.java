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

/**
 * This class manages button events in the voting pane
 * and updates the text labels in the GUI front-end
 *
 * TODO: Allow scrolling through poll chart when more than 10 items are displayed
 * TODO: Add support for loading and saving data
 * TODO: Add support for adding new entries
 *
 * @author Richard
 *
 */

public class VoteManager {
    
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

    public VoteManager() {
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

    /**
     * @return String of current beer displayed.
     */
    public String getCurrentBeer() { return beerTypes.get(currentBeer); }

    /**
     * @return Number of votes for the current beer.
     */
    public String getCurrentVotes() { return beerTypeLikes.get(beerTypes.get(currentBeer)).toString(); }

    /**
     * Adds beer to list with current number of votes
     *
     * @param newBeer,
     *            String containing name of new beer
     * @param votes,
     *            Number of votes associated with beer
     */
    public void addBeer(String newBeer, int votes) {
        beerTypeLikes.put(newBeer,votes);
        beerTypes.add(newBeer);
    }

    /**
     * Sets up the navigation button to scroll left through the list
     *
     * @param textObject, Text object that displays the current beer
     * @param img, Graphic used to represent the button
     * @return returns Button left
     */
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

    /**
     * Sets up the navigation button to scroll right through the list
     *
     * @param textObject, Text object that displays the current beer
     * @param img, Graphic used to represent the button
     * @return returns Button right
     */
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

    /**
     * Sets up the like button used to upvote
     *
     * @param likes, Text object that displays number of votes
     * @param img, Graphic used to represent the button
     * @return returns Button likesButton
     */
    public Button createLikeButton(Text likes, ImageView img) {
        Button likeButton = new Button("", img);
        likesDisplay = likes;
        likeButton.setBackground(Background.EMPTY);

        // Find the highest vote and set the global variable used for drawing
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
                    // Update all chart elements if highestVote increases
                    highestVote = beerTypeLikes.get(beerToUpvote);
                    updatePollChart(beerToUpvote,true);
                }
                else {
                    // Chart element is still within range, only update one element
                    updatePollChart(beerToUpvote,false);
                }
            }
        });
        return likeButton;
    }

    /**
     * Sets the poll chart to visualize the current number of votes.
     * The chart scales to the max number of votes for a beer.
     * MAX_BAR_HEIGHT defines the heighest bar
     * FIXED_BAR_WIDTH defines the width of each bar
     *
     * @return returns an HBox container
     */
    public HBox createPollChart(){
        final int FIXED_BAR_WIDTH = 55;
        final int MAX_BEERS_DISPLAYED = 10;

        int beersToDisplay = beerTypes.size();

        if (beersToDisplay > MAX_BEERS_DISPLAYED) {
            beersToDisplay = MAX_BEERS_DISPLAYED;
        }

        for (int i=0; i<beersToDisplay; i++) {
            int rectHeight = beerTypeLikes.get(beerTypes.get(i)) * MAX_BAR_HEIGHT / highestVote;
            String current = beerTypes.get(i);
            beerVotesBar.put(current, new Rectangle(FIXED_BAR_WIDTH,rectHeight));
            beerVotesBar.get(current).setFill(unselectedColour);

            // Set action event for when the chart element is clicked
            beerVotesBar.get(current).setOnMouseClicked(event -> goToElement(current));

            // Add all chart elements to pane
            pollsPane.getChildren().add(beerVotesBar.get(beerTypes.get(i)));
        }

        // Highlight the current element
        beerVotesBar.get(beerTypes.get(currentBeer)).setFill(selectedColour);

        pollsPane.setSpacing(10);
        pollsPane.setAlignment(Pos.CENTER);
        return pollsPane;
    }

    /**
     * Changes the current beer displayed to the index provided and
     * updates all display values in the GUI front-end
     *
     * @param newIndex, a String value used to look up the next beer
     */
    private void goToElement(String newIndex) {
        beerVotesBar.get(beerTypes.get(currentBeer)).setFill(unselectedColour);
        beerVotesBar.get(newIndex).setFill(selectedColour);
        currentBeer = beerTypes.indexOf(newIndex);

        display.setText(beerTypes.get(currentBeer));
        likesDisplay.setText(beerTypeLikes.get(beerTypes.get(currentBeer)).toString() + " Votes");
    }

    /**
     * Changes the current beer displayed to the index provided and
     * updates all display values in the GUI front-end
     *
     * @param newIndex, an int index used to look up the next beer
     */
    private void goToElement(int newIndex) {
        beerVotesBar.get(beerTypes.get(currentBeer)).setFill(unselectedColour);
        beerVotesBar.get(beerTypes.get(newIndex)).setFill(selectedColour);
        currentBeer = newIndex;

        display.setText(beerTypes.get(currentBeer));
        likesDisplay.setText(beerTypeLikes.get(beerTypes.get(currentBeer)).toString() + " Votes");
    }

    /**
     * Updates the poll chart elements to its new heights.
     *
     * @param beerToUpdate, String value used to look up the beer to update
     * @param updateAll, Boolean that will trigger the method to update all elements
     */
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
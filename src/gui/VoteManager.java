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
 * TODO: Add support for loading and saving data
 * TODO: Add support for adding new entries
 *
 * @author Richard
 *
 */

public class VoteManager {
    
    private final Map<String, Integer> beerTypeLikes    = new HashMap<String, Integer>();
    private final List<String> beerTypes                = new ArrayList<String>();
    private final List<Rectangle> beerVotesBar          = new ArrayList<>();
    private final HBox pollsPane                        = new HBox();

    private final int MAX_BAR_HEIGHT                    = 100;
    private final int MIN_BAR_HEIGHT                    = 4;
    private final int MAX_BEERS_DISPLAYED               = 10;
    private final Color UNSELECTED                      = Color.web("006B68");
    private final Color SELECTED                        = Color.web("06D3CE");

    private Text display, likesDisplay;
    private int currentBeer = 0;
    private int lowestIndexed = 0;
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
        addBeer("11. Kronenberg",5);
        addBeer("12. Steamworks IPA",7);
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
     * @return Returns the chart
     */
    public HBox getPollChart() { return pollsPane; }

    /**
     * Adds beer to list with current number of votes
     *
     * @param newBeer, String containing name of new beer
     * @param votes, Number of votes associated with beer
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
                    goToElement(currentBeer - 1,false);
                } else if (currentBeer == 0) {
                    lowestIndexed = beerTypes.size() - MAX_BEERS_DISPLAYED;
                    goToElement(beerTypes.size() - 1,true);
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
                    goToElement(currentBeer + 1,false);
                } else if (currentBeer == beerTypes.size() - 1) {
                    lowestIndexed = 0;
                    goToElement(0,true);
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
                    updatePollChart(currentBeer,true);
                }
                else {
                    // Chart element is still within range, only update one element
                    updatePollChart(currentBeer,false);
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

        int beersToDisplay = beerTypes.size();

        if (beersToDisplay > MAX_BEERS_DISPLAYED) {
            beersToDisplay = MAX_BEERS_DISPLAYED;
        }

        for (int i=0; i<beersToDisplay; i++) {
            int rectHeight = beerTypeLikes.get(beerTypes.get(i)) * (MAX_BAR_HEIGHT-MIN_BAR_HEIGHT) / highestVote + MIN_BAR_HEIGHT;
            beerVotesBar.add(new Rectangle(FIXED_BAR_WIDTH,rectHeight));
            beerVotesBar.get(i).setFill(UNSELECTED);

            // Set action event for when the chart element is clicked
            int finalI = i;
            beerVotesBar.get(i).setOnMouseClicked(event -> goToElement(finalI+lowestIndexed,false));

            // Add all chart elements to pane
            pollsPane.getChildren().add(beerVotesBar.get(i));
        }

        // Highlight the current element
        beerVotesBar.get(currentBeer).setFill(SELECTED);

        pollsPane.setSpacing(10);
        pollsPane.setAlignment(Pos.CENTER);
        pollsPane.setMinHeight(MAX_BAR_HEIGHT);
        return pollsPane;
    }

    /**
     * Changes the current beer displayed to the index provided and
     * updates all display values in the GUI front-end
     *
     * @param newIndex, an int index used to look up the next beer
     * @param wrapAround, a boolean to specify if the list has reached
     *                    the end, and will start from the beginning
     */
    private void goToElement(int newIndex, boolean wrapAround) {
        if (wrapAround) {
            // Shift charts, must update all elements in chart
            updatePollChart(currentBeer,true);

            // Swap highlighted element from last to first
            if (lowestIndexed == 0) {
                beerVotesBar.get(MAX_BEERS_DISPLAYED-1).setFill(UNSELECTED);
                beerVotesBar.get(0).setFill(SELECTED);
            }
            // Swap highlighted element from first to last
            else {
                beerVotesBar.get(0).setFill(UNSELECTED);
                beerVotesBar.get(MAX_BEERS_DISPLAYED-1).setFill(SELECTED);
            }

        // Next element is within the range that is currently displayed
        } else if (newIndex >= lowestIndexed && newIndex < lowestIndexed + MAX_BEERS_DISPLAYED) {
            beerVotesBar.get(currentBeer-lowestIndexed).setFill(UNSELECTED);
            beerVotesBar.get(newIndex-lowestIndexed).setFill(SELECTED);

        // Next element is lower than the range that is currently displayed
        } else if (newIndex < lowestIndexed) {
            lowestIndexed--;
            updatePollChart(currentBeer,true);

        // Next element is higher than the range that is currently displayed
        } else if (newIndex >= lowestIndexed + MAX_BEERS_DISPLAYED) {
            lowestIndexed++;
            updatePollChart(currentBeer,true);
        }

        currentBeer = newIndex;
        display.setText(beerTypes.get(currentBeer));
        likesDisplay.setText(beerTypeLikes.get(beerTypes.get(currentBeer)).toString() + " Votes");
    }

    /**
     * Updates the poll chart elements to its new heights.
     *
     * @param beerToUpdate, an int value used to look up the index of the beer to update
     * @param updateAll, Boolean that will trigger the method to update all elements
     */
    private void updatePollChart(int beerToUpdate, boolean updateAll) {
        final int maxBeersDisplayed = 10;
        int beersToDisplay = beerTypes.size();

        if (beersToDisplay > maxBeersDisplayed) {
            beersToDisplay = maxBeersDisplayed;
        }

        if (updateAll) {
            for (int i=0; i<beersToDisplay; i++) {
                int relativeIndex = lowestIndexed + i;
                int newHeight = beerTypeLikes.get(beerTypes.get(relativeIndex)) * (MAX_BAR_HEIGHT-MIN_BAR_HEIGHT) / highestVote + MIN_BAR_HEIGHT;
                beerVotesBar.get(i).setHeight(newHeight);
            }
        }
        else {
            int newHeight = beerTypeLikes.get(beerTypes.get(beerToUpdate)) * (MAX_BAR_HEIGHT-MIN_BAR_HEIGHT) / highestVote + MIN_BAR_HEIGHT;
            beerVotesBar.get(beerToUpdate-lowestIndexed).setHeight(newHeight);
        }
    }
}
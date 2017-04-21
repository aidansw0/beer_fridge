package gui;

import backend.KeyCardListener;
import backend.SaveData;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.*;

/**
 * This class manages button events in the voting pane
 * and updates the text labels in the GUI front-end
 *
 * @author Richard
 *
 */

public class VoteManager {
    
    private final Map<String, Integer> beerTypeLikes    = new HashMap<String, Integer>();
    private final List<String> beerTypes                = new ArrayList<String>();
    private final List<Rectangle> beerVotesBar          = new ArrayList<>();
    private final HBox pollsPane                        = new HBox();
    private SaveData saveData;
    private KeyCardListener keyCardListener;

    private final int MAX_BAR_HEIGHT                    = 100;
    private final int FIXED_BAR_WIDTH                   = 55;
    private final int MIN_BAR_HEIGHT                    = 4;
    private final int MAX_BEERS_DISPLAYED               = 10;
    private final Color UNSELECTED                      = Color.web("006B68");
    private final Color SELECTED                        = Color.web("06D3CE");

    private Text display, likesDisplay;
    private int currentBeer = 0;
    private int lowestIndexed = 0;
    private int highestVote = 10;

    public VoteManager() {
        // Read data from saved file
        saveData = new SaveData(beerTypeLikes, beerTypes);
    }

    /**
     * @return String of current beer displayed.
     */
    public String getCurrentBeer() {
        String retval = " ";
        if (saveData.isDataReady()) {
            retval = beerTypes.get(currentBeer);
        }
        return retval;
    }

    /**
     * @return Number of votes for the current beer.
     */
    public String getCurrentVotes() {
        String retval = " ";
        if (saveData.isDataReady()) {
            retval = beerTypeLikes.get(beerTypes.get(currentBeer)).toString();
        }
        return retval;
    }

    /**
     * @return Returns the chart
     */
    public HBox getPollChart() { return pollsPane; }

    /**
     * Adds beer to list with current number of votes and redraw
     * rectangles. Will also check if duplicates are found and
     * return a boolean.
     *
     * @param newBeer, String containing name of new beer
     * @param votes, Number of votes associated with beer
     * @return a boolean to describe whether the entry exists
     */
    public boolean addBeer(String newBeer, int votes) {
        int index = beerTypes.size();

        // Check if beer exists
        if (beerTypeLikes.containsKey(newBeer)) {
            for (int i=0; i<beerTypes.size(); i++) {
                if (beerTypes.get(i).equals(newBeer)) {
                    goToElement(i,false);
                    break;
                }
            }
            return false;
        }

        beerTypeLikes.put(newBeer,votes);
        beerTypes.add(newBeer);

        if (beerTypes.size() <= MAX_BEERS_DISPLAYED) {
            int rectHeight = votes * (MAX_BAR_HEIGHT-MIN_BAR_HEIGHT) / highestVote + MIN_BAR_HEIGHT;
            beerVotesBar.add(new Rectangle(FIXED_BAR_WIDTH,rectHeight));

            // Set action event for when the chart element is clicked
            beerVotesBar.get(index).setOnMouseClicked(event -> goToElement(index+lowestIndexed,false));
            beerVotesBar.get(index).setFill(UNSELECTED);

            // Add all chart elements to pane
            pollsPane.getChildren().add(beerVotesBar.get(index));

            // Highlight the current element
            beerVotesBar.get(currentBeer).setFill(SELECTED);
            display.setText(beerTypes.get(currentBeer));
            likesDisplay.setText(beerTypeLikes.get(beerTypes.get(currentBeer)).toString() + " Votes");
        }

        saveToFile();
        return true;
    }

    /**
     * Attempts to save to file
     */
    public void saveToFile() {
        try {
            saveData.writeData();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                if (beerTypes.size() > 1) {
                    if (currentBeer > 0) {
                        goToElement(currentBeer - 1, false);
                    } else if (currentBeer == 0) {
                        if (beerTypes.size() > MAX_BEERS_DISPLAYED) {
                            lowestIndexed = beerTypes.size() - MAX_BEERS_DISPLAYED;
                        }

                        goToElement(beerTypes.size() - 1, true);
                    }
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
                if (beerTypes.size() > 1) {
                    if (currentBeer < beerTypes.size() - 1) {
                        goToElement(currentBeer + 1, false);
                    } else if (currentBeer == beerTypes.size() - 1) {
                        lowestIndexed = 0;
                        goToElement(0, true);
                    }
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
    public Button createLikeButton(Text likes, ImageView img, KeyCardListener listener) {
        Button likeButton = new Button("", img);
        likesDisplay = likes;
        keyCardListener = listener;
        likeButton.setBackground(Background.EMPTY);

        // Find the highest vote and set the global variable used for drawing
        for (int i=0; i<beerTypeLikes.size(); i++) {
            if (beerTypeLikes.get(beerTypes.get(i)) > highestVote) {
                highestVote = beerTypeLikes.get(beerTypes.get(i));
            }
        }

        likeButton.setOnAction(event -> {
            if (saveData.isDataReady() && keyCardListener.checkKeyValid()) {
                String beerToUpvote = beerTypes.get(currentBeer);
                beerTypeLikes.put(beerToUpvote, beerTypeLikes.get(beerToUpvote) + 1);
                likesDisplay.setText(beerTypeLikes.get(beerTypes.get(currentBeer)).toString() + " Votes");

                if (beerTypeLikes.get(beerToUpvote) > highestVote) {
                    // Update all chart elements if highestVote increases
                    highestVote = beerTypeLikes.get(beerToUpvote);
                    updatePollChart(currentBeer, true);
                } else {
                    // Chart element is still within range, only update one element
                    updatePollChart(currentBeer, false);
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
        if (saveData.isDataReady()) {
            beerVotesBar.get(currentBeer).setFill(SELECTED);
        }

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

            if (beerVotesBar.size() <= MAX_BEERS_DISPLAYED) {
                if (newIndex == 0) {
                    beerVotesBar.get(beerVotesBar.size() - 1).setFill(UNSELECTED);
                    beerVotesBar.get(0).setFill(SELECTED);
                }
                else {
                    beerVotesBar.get(0).setFill(UNSELECTED);
                    beerVotesBar.get(beerVotesBar.size() - 1).setFill(SELECTED);
                }
            }
            // Swap highlighted element from last to first
            else if (lowestIndexed == 0) {
                beerVotesBar.get(beerVotesBar.size() - 1).setFill(UNSELECTED);
                beerVotesBar.get(0).setFill(SELECTED);
            }
            // Swap highlighted element from first to last
            else {
                beerVotesBar.get(0).setFill(UNSELECTED);
                beerVotesBar.get(beerVotesBar.size() - 1).setFill(SELECTED);
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
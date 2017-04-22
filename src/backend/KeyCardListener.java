package backend;

import javafx.beans.property.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class listens to the key access card scanner
 * and verifies if the card is valid
 *
 * @author Richard
 */

public class KeyCardListener {
    private final Set<String> keyCardID = new HashSet<>();
    private final Set<String> adminID = new HashSet<>();
    private final Timer timer = new Timer();

    private String readInput = "";
    private String newCardBuffer = "";
    private final SimpleStringProperty cardHintText;
    private final ReadOnlyBooleanWrapper keyVerified;
    private final ReadOnlyBooleanWrapper keyNotDuplicate;
    private final ReadOnlyBooleanWrapper newAttempt;

    private final int KEY_CARD_ID_LENGTH = 25;
    private final int KEY_EXPIRY_TIME = 60000; // in ms
    private final int ATTEMPT_EXPIRY_TIME = 3000; // in ms

    public KeyCardListener() {
        // Added for testing - Richard's access card
        adminID.add("0000000000000000708c14057");
//        adminID.add("1234123412341234123412345");

        cardHintText = new SimpleStringProperty("Please Scan Card ...");
        keyVerified = new ReadOnlyBooleanWrapper(false);
        keyNotDuplicate = new ReadOnlyBooleanWrapper(false);
        newAttempt = new ReadOnlyBooleanWrapper(false);
    }

    public StringProperty getHintText() { return cardHintText; }

    public ReadOnlyBooleanProperty keyNotUsedProperty() { return keyNotDuplicate.getReadOnlyProperty(); }

    public ReadOnlyBooleanProperty adminVerifiedProperty() { return keyVerified.getReadOnlyProperty(); }

    /**
     * @return Returns true if key card was verified but does not toggle the variable
     */
    public boolean checkKeyNotUsedReadOnly() {
        if (keyNotDuplicate.get()) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * @return Returns true if key card was verified and toggles to false
     */
    public boolean checkKeyNotUsed() {
        if (keyNotDuplicate.get()) {
            keyNotDuplicate.set(false);
            keyVerified.set(false);
            cardHintText.set("Please Scan Card ...");
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * @return Returns true if key card was verified but does not toggle the variable
     */
    public boolean checkAdminValidReadOnly() {
        if (keyVerified.get()) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * @return Returns true if key card was verified and toggles to false
     */
    public boolean checkAdminValid() {
        if (keyVerified.get()) {
            keyVerified.set(false);
            keyNotDuplicate.set(false);
            cardHintText.set("Please Scan Card ...");
            return true;
        }
        else {
            return false;
        }
    }

    public void registerVote() {
        keyCardID.add(newCardBuffer);
    }

    /**
     * Event handler for key events used by the application for continuously
     * listening for key strokes
     *
     * @param event, KeyEvent must be passed in to check the event code
     */
    public void handleEvent(KeyEvent event) {
        if (event.getCode() == KeyCode.SLASH) {
            if (readInput.length() >= KEY_CARD_ID_LENGTH) {
                String keyCardRead = readInput.substring(readInput.length() - KEY_CARD_ID_LENGTH);

                // Administrative ID
                if (adminID.contains(keyCardRead.toLowerCase())) {
                    timer.schedule(new ExpireAccess(keyVerified,cardHintText), KEY_EXPIRY_TIME);

                    System.out.print("Admin Key Found: ");
                    cardHintText.set("Admin Card Verified");
                    keyVerified.set(true);
                    keyNotDuplicate.set(true);

                // Already voted
                } else if (keyCardID.contains(keyCardRead)){
                    System.out.print("Already Voted: ");
                    cardHintText.set("Already Voted");
                    keyVerified.set(false);
                    keyNotDuplicate.set(false);
                }
                // New card
                else {
                    System.out.print("New Vote: ");
                    cardHintText.set("Press Thumb to Vote");
                    newCardBuffer = keyCardRead;
                    keyVerified.set(false);
                    keyNotDuplicate.set(true);
                }
                System.out.println(keyCardRead);
            }
            else {
                System.out.println("Key NOT valid: " + readInput);
                cardHintText.set("Invalid");
                keyVerified.set(false);
            }
            readInput = "";
        }
        else {
            if (!newAttempt.get()) {
                timer.schedule(new ExpireAttempt(newAttempt,cardHintText,keyVerified,keyNotDuplicate), ATTEMPT_EXPIRY_TIME);
                readInput = "";
            }

            newAttempt.set(true);

            readInput = readInput + event.getText();
            String temp = readInput;
            temp = temp.replaceAll(".",". ");
            cardHintText.set(temp);
        }
    }
}

class ExpireAccess extends TimerTask {

    private ReadOnlyBooleanWrapper keyVerified;
    private SimpleStringProperty cardHintText;

    public ExpireAccess(ReadOnlyBooleanWrapper keyVerified, SimpleStringProperty cardHintText) {
        this.keyVerified = keyVerified;
        this.cardHintText = cardHintText;
    }

    @Override
    public void run() {
        keyVerified.set(false);
        cardHintText.set("Please Scan Card ...");
    }
}

class ExpireAttempt extends TimerTask {

    private ReadOnlyBooleanWrapper newAttempt;
    private SimpleStringProperty cardHintText;
    private ReadOnlyBooleanWrapper keyVerified;
    private ReadOnlyBooleanWrapper keyNotDuplicate;

    public ExpireAttempt(ReadOnlyBooleanWrapper newAttempt, SimpleStringProperty cardHintText, ReadOnlyBooleanWrapper keyVerified, ReadOnlyBooleanWrapper keyNotDuplicate) {
        this.newAttempt = newAttempt;
        this.cardHintText = cardHintText;
        this.keyVerified = keyVerified;
        this.keyNotDuplicate = keyNotDuplicate;
    }

    @Override
    public void run() {
        if (keyVerified.get() || keyNotDuplicate.get()) {
            newAttempt.set(false);
        }
        else {
            newAttempt.set(false);
            cardHintText.set("Please Scan Card ...");
        }
    }
}
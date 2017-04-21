package backend;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
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
    private final Timer timer = new Timer();

    private String readInput = "";
    private final ReadOnlyBooleanWrapper keyNotVerified;

    private final int KEY_CARD_ID_LENGTH = 25;
    private final int KEY_EXPIRY_TIME = 60000; // in ms

    public KeyCardListener() {
        // Added for testing - Richard's access card
        keyCardID.add("0000000000000000708C14057");

        keyNotVerified = new ReadOnlyBooleanWrapper();
        keyNotVerified.set(true);
    }

    public ReadOnlyBooleanProperty keyNotVerifiedProperty() { return keyNotVerified.getReadOnlyProperty(); }

    /**
     * @return Returns true if key card was verified but does not toggle the variable
     */
    public boolean checkKeyValidReadOnly() {
        if (!keyNotVerified.get()) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * @return Returns true if key card was verified and toggles to false
     */
    public boolean checkKeyValid() {
        if (!keyNotVerified.get()) {
            keyNotVerified.set(true);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Event handler for key events used by the application for continuously
     * listening for key strokes
     *
     * @param event, KeyEvent must be passed in to check the event code
     */
    public void handleEvent(KeyEvent event) {
        if (event.getCode() == KeyCode.SLASH) {
            String keyCardRead = readInput.substring(readInput.length()-KEY_CARD_ID_LENGTH);

            if (keyCardID.contains(keyCardRead)) {
                timer.schedule(new ExpireAccess(keyNotVerified),KEY_EXPIRY_TIME);

                System.out.print("Key Found: ");
                keyNotVerified.set(false);
            }
            else {
                System.out.print("Key NOT found: ");
                keyNotVerified.set(true);
            }

            System.out.println(keyCardRead);
            readInput = "";
        }
        else {
            readInput = readInput + event.getText();
        }
    }
}

class ExpireAccess extends TimerTask {

    private ReadOnlyBooleanWrapper disableAccess;

    public ExpireAccess(ReadOnlyBooleanWrapper disableAccess) {
        this.disableAccess = disableAccess;
    }

    @Override
    public void run() {
        disableAccess.set(true);
    }
}
package backend;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * This class listens to the key access card scanner
 * and verifies if the card is valid
 *
 * @author Richard
 */

public class KeyCardListener {
    private final Set<String> keyCardID = new HashSet<>();

    private long timeScanned;
    private String readInput = "";
    private boolean keyVerified = false;

    private final int KEY_CARD_ID_LENGTH = 25;
    private final int KEY_EXPIRY_TIME = 10000; // in ms

    public KeyCardListener() {
        // Added for testing - Richard's access card
        keyCardID.add("0000000000000000708C14057");
    }

    /**
     * @return Returns true if key card was verified but does not toggle the variable
     */
    public boolean checkKeyValidReadOnly() {
        long elapsed = System.currentTimeMillis() - timeScanned;

        if (keyVerified && elapsed < KEY_EXPIRY_TIME) {
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
        long elapsed = System.currentTimeMillis() - timeScanned;

        if (keyVerified && elapsed < KEY_EXPIRY_TIME) {
            keyVerified = false;
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
                System.out.print("Key Found: ");
                timeScanned = System.currentTimeMillis();
                keyVerified = true;
            }
            else {
                System.out.print("Key NOT found: ");
                keyVerified = false;
            }

            System.out.println(keyCardRead);
            readInput = "";
        }
        else {
            readInput = readInput + event.getText();
        }
    }
}

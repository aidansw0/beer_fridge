package backend;

import com.sun.tools.doclets.formats.html.SourceToHTMLConverter;
import javafx.event.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by rchen on 2017-04-20.
 */

public class KeyCardListener {
    private final Set<String> keyCardID = new HashSet<>();

    private String readInput = "";
    private boolean keyVerified = false;

    public KeyCardListener() {
        // Added for testing - Richard's access card
        keyCardID.add("0000000000000000708C14057");
    }

    /**
     * @return Returns true if key card was verified but does not toggle the variable
     */
    public boolean checkKeyValidReadOnly() { return keyVerified; }

    /**
     * @return Returns true if key card was verified and toggles to false
     */
    public boolean checkKeyValid() {
        if (keyVerified) {
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
            if (keyCardID.contains(readInput)) {
                System.out.print("Key Found: ");
                keyVerified = true;
            }
            else {
                System.out.print("Key NOT found: ");
                keyVerified = false;
            }

            System.out.println(readInput);
            readInput = "";
        }
        else {
            readInput = readInput + event.getText();
        }
    }
}

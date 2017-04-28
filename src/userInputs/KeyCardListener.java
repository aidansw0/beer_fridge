package userInputs;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.Timer;
import java.util.TimerTask;

import dataManagement.DataManager;

/**
 * This class listens to the key access card scanner
 * and verifies if the card is valid
 *
 * @author Richard
 */

public class KeyCardListener {
    private Timer timer = new Timer();
    
    private final DataManager dataManager;

    private String readInput = "";
    private String newCardBuffer = "";
    private final SimpleStringProperty cardHintText, adminHintText;
    private final ReadOnlyBooleanWrapper adminKeyVerified;
    private final ReadOnlyBooleanWrapper regularKeyVerified;
    private final ReadOnlyBooleanWrapper newAttempt;

    private static final int KEY_CARD_ID_LENGTH = 25;
    private static final int ACCESS_EXPIRY_TIME = 60000; // in ms
    private static final int ATTEMPT_EXPIRY_TIME = 3000; // in ms

    public KeyCardListener(DataManager dataManager) {
        this.dataManager = dataManager;

        this.dataManager.setAdmin("0000000000000000708c14057", true); // Richard
        this.dataManager.setAdmin("0000000000000000708c14054", true); // Aidan

        cardHintText = new SimpleStringProperty("Please Scan Card ...");
        adminHintText = new SimpleStringProperty("Please Scan Card ...");
        adminKeyVerified = new ReadOnlyBooleanWrapper(false);
        regularKeyVerified = new ReadOnlyBooleanWrapper(false);
        newAttempt = new ReadOnlyBooleanWrapper(false);
    }

    public StringProperty getHintText() { return cardHintText; }

    public StringProperty getAdminHintText() { return adminHintText; }

    public void resetAdminHint() { adminHintText.set("Please Scan Card ..."); }

    public ReadOnlyBooleanProperty regularKeyVerifiedProperty() { return regularKeyVerified.getReadOnlyProperty(); }

    public ReadOnlyBooleanProperty adminKeyVerifiedProperty() { return adminKeyVerified.getReadOnlyProperty(); }

    public void registerVote() {
        dataManager.setVoted(newCardBuffer, true);
    }

    /**
     * @param consumeAccess, consumes the access if set to true
     * @return Returns true if a regular key card was verified
     */
    public boolean checkRegularKeyVerified(boolean consumeAccess) {
        boolean retVal = regularKeyVerified.get();

        if (consumeAccess) {
            timer.cancel();
            adminKeyVerified.set(false);
            regularKeyVerified.set(false);
            cardHintText.set("Please Scan Card ...");
        }

        return retVal;
    }

    /**
     * @param consumeAccess, consumes the access if set to true
     * @return Returns true if admin key card was verified and toggles to false
     */
    public boolean checkAdminKeyVerified(boolean consumeAccess) {
        boolean retVal = adminKeyVerified.get();

        if (consumeAccess) {
            timer.cancel();
            adminKeyVerified.set(false);
            regularKeyVerified.set(false);
            cardHintText.set("Please Scan Card ...");
        }

        return retVal;
    }

    /**
     * Event handler for key events used by the application for continuously
     * listening for key strokes
     *
     * @param keyEvent, KeyEvent must be passed in to check the event code
     */
    public void handleEvent(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.SLASH) {
            newAttempt.set(false);
            timer.cancel();

            if (readInput.length() >= KEY_CARD_ID_LENGTH) {
                String keyCardRead = readInput.substring(readInput.length() - KEY_CARD_ID_LENGTH);

                // admin access
                if (dataManager.checkAdmin(keyCardRead)) {
                    timer = new Timer();
                    timer.schedule(new ExpireAccess(), ACCESS_EXPIRY_TIME);

                    System.out.print("Admin Key Found: ");
                    cardHintText.set("Admin Card Verified");
                    adminKeyVerified.set(true);
                    regularKeyVerified.set(true);

                // already voted
                } else if (dataManager.checkVoted(keyCardRead)) {
                    System.out.print("Already Voted: ");
                    cardHintText.set("Already Voted");
                    adminKeyVerified.set(false);
                    regularKeyVerified.set(false);

                // new card
                } else {
                    timer = new Timer();
                    timer.schedule(new ExpireAccess(), ACCESS_EXPIRY_TIME);

                    System.out.print("New Vote: ");
                    cardHintText.set("Press Thumb to Vote");
                    newCardBuffer = keyCardRead;
                    adminKeyVerified.set(false);
                    regularKeyVerified.set(true);
                }

                System.out.println(keyCardRead);
                
            } else {
                System.out.println("Key NOT valid: " + readInput);
                cardHintText.set("Invalid");
                adminKeyVerified.set(false);
            }
            readInput = "";
        }
        else if (keyEvent.getCode() != KeyCode.ENTER && keyEvent.getCode() != KeyCode.ESCAPE){
            if (!newAttempt.get()) {
                timer.cancel();
                timer = new Timer();
                timer.schedule(new ExpireAttempt(), ATTEMPT_EXPIRY_TIME);

                readInput = "";
                adminKeyVerified.set(false);
                regularKeyVerified.set(false);
            }

            newAttempt.set(true);
            readInput = readInput + keyEvent.getText();
            cardHintText.set(readInput.replaceAll(".",". "));
        }
    }

    /**
     * Event handler for key events used by the application for continuously
     * listening for key strokes
     *
     * @param keyEvent, KeyEvent must be passed in to check the event code
     */
    public boolean handleNewAdmin(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.SLASH) {
            if (readInput.length() >= KEY_CARD_ID_LENGTH) {
                String keyCardRead = readInput.substring(readInput.length() - KEY_CARD_ID_LENGTH);

                // admin access
                if (dataManager.checkAdmin(keyCardRead)) {
                    System.out.print("Already an Admin: ");
                    adminHintText.set("Already an Admin");

                    // new card
                } else {
                    dataManager.setAdmin(keyCardRead,true);
                    readInput = "";

                    System.out.print("New Admin Added: ");
                    adminHintText.set("New Admin Added");
                    return true;
                }

                System.out.println(keyCardRead);

            } else {
                System.out.println("Key NOT valid: " + readInput);
                adminHintText.set("Invalid");
            }
            readInput = "";
        }
        else if (keyEvent.getCode() != KeyCode.ENTER && keyEvent.getCode() != KeyCode.ESCAPE){
            readInput = readInput + keyEvent.getText();
            adminHintText.set(readInput.replaceAll(".",". "));
        }
        return false;
    }

    /**
     * Event handler for key events used by the application for continuously
     * listening for key strokes
     *
     * @param keyEvent, KeyEvent must be passed in to check the event code
     */
    public boolean handleRemoveAdmin(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.SLASH) {
            if (readInput.length() >= KEY_CARD_ID_LENGTH) {
                String keyCardRead = readInput.substring(readInput.length() - KEY_CARD_ID_LENGTH);

                // admin access
                if (dataManager.checkAdmin(keyCardRead)) {
                    dataManager.setAdmin(keyCardRead,false);
                    readInput = "";

                    System.out.print("Admin Removed: ");
                    adminHintText.set("Admin Removed");

                    return true;
                    // new card
                } else {

                    System.out.print("Not an Admin: ");
                    adminHintText.set("Not an Admin");
                }

                System.out.println(keyCardRead);

            } else {
                System.out.println("Key NOT valid: " + readInput);
                adminHintText.set("Invalid");
            }
            readInput = "";
        }
        else if (keyEvent.getCode() != KeyCode.ENTER && keyEvent.getCode() != KeyCode.ESCAPE){
            readInput = readInput + keyEvent.getText();
            adminHintText.set(readInput.replaceAll(".",". "));
        }
        return false;
    }

    private class ExpireAccess extends TimerTask {
        @Override
        public void run() {
            // If no action is executed, expire the access
            adminKeyVerified.set(false);
            regularKeyVerified.set(false);
            cardHintText.set("Please Scan Card ...");
        }
    }

    private class ExpireAttempt extends TimerTask {
        @Override
        public void run() {
            newAttempt.set(false);
            // If verification unsuccessful, expire the attempt
            if (!adminKeyVerified.get() && !regularKeyVerified.get()) {
                cardHintText.set("Please Scan Card ...");
            }
        }
    }
}

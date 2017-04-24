package backend;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
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
    //private final Set<String> keyCardID = new HashSet<>();
    //private final Set<String> adminID = new HashSet<>();
    private final Timer timer = new Timer();
    
    private final SaveData saveData;

    private String readInput = "";
    private String newCardBuffer = "";
    private final SimpleStringProperty cardHintText;
    private final ReadOnlyBooleanWrapper adminKeyVerified;
    private final ReadOnlyBooleanWrapper regularKeyVerified;
    private final ReadOnlyBooleanWrapper newAttempt;

    private static final int KEY_CARD_ID_LENGTH = 25;
    private static final int ACCESS_EXPIRY_TIME = 30000; // in ms
    private static final int ATTEMPT_EXPIRY_TIME = 3000; // in ms

    public KeyCardListener(SaveData saveData) {
        // Added for testing - Richard's access card
        //adminID.add("0000000000000000708c14057");
        this.saveData = saveData;

        cardHintText = new SimpleStringProperty("Please Scan Card ...");
        adminKeyVerified = new ReadOnlyBooleanWrapper(false);
        regularKeyVerified = new ReadOnlyBooleanWrapper(false);
        newAttempt = new ReadOnlyBooleanWrapper(false);
    }

    public StringProperty getHintText() { return cardHintText; }

    public ReadOnlyBooleanProperty regularKeyVerifiedProperty() { return regularKeyVerified.getReadOnlyProperty(); }

    public ReadOnlyBooleanProperty adminKeyVerifiedProperty() { return adminKeyVerified.getReadOnlyProperty(); }

    public void registerVote() {
        //keyCardID.add(newCardBuffer);
        saveData.setVoted(newCardBuffer, true);
    }

    /**
     * @param consumeAccess, consumes the access if set to true
     * @return Returns true if a regular key card was verified
     */
    public boolean checkRegularKeyVerified(boolean consumeAccess) {
        if (regularKeyVerified.get()) {
            if (consumeAccess) {
                adminKeyVerified.set(false);
                regularKeyVerified.set(false);
                cardHintText.set("Please Scan Card ...");
            }
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * @param consumeAccess, consumes the access if set to true
     * @return Returns true if admin key card was verified and toggles to false
     */
    public boolean checkAdminKeyVerified(boolean consumeAccess) {
        if (adminKeyVerified.get()) {
            if (consumeAccess) {
                adminKeyVerified.set(false);
                regularKeyVerified.set(false);
                cardHintText.set("Please Scan Card ...");
            }
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
     * @param keyEvent, KeyEvent must be passed in to check the event code
     */
    public void handleEvent(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.SLASH) {
            if (readInput.length() >= KEY_CARD_ID_LENGTH) {
                String keyCardRead = readInput.substring(readInput.length() - KEY_CARD_ID_LENGTH);

//                // Administrative ID
//                if (adminID.contains(keyCardRead.toLowerCase())) {
//                    timer.schedule(new ExpireAccess(adminKeyVerified, regularKeyVerified,cardHintText), ACCESS_EXPIRY_TIME);
//
//                    System.out.print("Admin Key Found: ");
//                    cardHintText.set("Admin Card Verified");
//                    adminKeyVerified.set(true);
//                    regularKeyVerified.set(true);
//
//                // Already voted
//                } else if (keyCardID.contains(keyCardRead.toLowerCase())){
//                    System.out.print("Already Voted: ");
//                    cardHintText.set("Already Voted");
//                    adminKeyVerified.set(false);
//                    regularKeyVerified.set(false);
//                }
//                // New card
//                else {
//                    timer.schedule(new ExpireAccess(adminKeyVerified, regularKeyVerified,cardHintText), ACCESS_EXPIRY_TIME);
//
//                    System.out.print("New Vote: ");
//                    cardHintText.set("Press Thumb to Vote");
//                    newCardBuffer = keyCardRead.toLowerCase();
//                    adminKeyVerified.set(false);
//                    regularKeyVerified.set(true);
//                }
//                System.out.println(keyCardRead);
                
                    // admin access
                    if (saveData.checkAdmin(keyCardRead)) {
                        timer.schedule(new ExpireAccess(adminKeyVerified, regularKeyVerified,cardHintText), ACCESS_EXPIRY_TIME);

                        System.out.print("Admin Key Found: ");
                        cardHintText.set("Admin Card Verified");
                        adminKeyVerified.set(true);
                        regularKeyVerified.set(true);
                        
                    // already voted    
                    } else if (saveData.checkVoted(keyCardRead)) {
                        System.out.print("Already Voted: ");
                        cardHintText.set("Already Voted");
                        adminKeyVerified.set(false);
                        regularKeyVerified.set(false);
                        
                    // new card    
                    } else {
                        timer.schedule(new ExpireAccess(adminKeyVerified, regularKeyVerified,cardHintText), ACCESS_EXPIRY_TIME);

                        System.out.print("New Vote: ");
                        cardHintText.set("Press Thumb to Vote");
                        newCardBuffer = keyCardRead;
                        adminKeyVerified.set(false);
                        regularKeyVerified.set(true);
                    }
                    
                
                
            } else {
                System.out.println("Key NOT valid: " + readInput);
                cardHintText.set("Invalid");
                adminKeyVerified.set(false);
            }
            readInput = "";
        }
        else if (keyEvent.getCode() != KeyCode.ENTER && keyEvent.getCode() != KeyCode.ESCAPE){
            if (!newAttempt.get()) {
                timer.schedule(new ExpireAttempt(newAttempt,cardHintText, adminKeyVerified, regularKeyVerified), ATTEMPT_EXPIRY_TIME);
                readInput = "";
                adminKeyVerified.set(false);
                regularKeyVerified.set(false);
            }

            newAttempt.set(true);
            readInput = readInput + keyEvent.getText();
            cardHintText.set(readInput.replaceAll(".",". "));
        }
    }
}

class ExpireAccess extends TimerTask {

    private ReadOnlyBooleanWrapper adminKeyVerified;
    private ReadOnlyBooleanWrapper regularKeyVerified;
    private SimpleStringProperty cardHintText;

    public ExpireAccess(ReadOnlyBooleanWrapper adminKeyVerified, ReadOnlyBooleanWrapper regularKeyVerified, SimpleStringProperty cardHintText) {
        this.adminKeyVerified = adminKeyVerified;
        this.regularKeyVerified = regularKeyVerified;
        this.cardHintText = cardHintText;
    }

    @Override
    public void run() {
        // If no action is executed, expire the access
        adminKeyVerified.set(false);
        regularKeyVerified.set(false);
        cardHintText.set("Please Scan Card ...");
    }
}

class ExpireAttempt extends TimerTask {

    private ReadOnlyBooleanWrapper newAttempt;
    private ReadOnlyBooleanWrapper adminKeyVerified;
    private ReadOnlyBooleanWrapper regularKeyVerified;
    private SimpleStringProperty cardHintText;

    public ExpireAttempt(ReadOnlyBooleanWrapper newAttempt, SimpleStringProperty cardHintText, ReadOnlyBooleanWrapper adminKeyVerified, ReadOnlyBooleanWrapper regularKeyVerified) {
        this.newAttempt = newAttempt;
        this.adminKeyVerified = adminKeyVerified;
        this.regularKeyVerified = regularKeyVerified;
        this.cardHintText = cardHintText;
    }

    @Override
    public void run() {
        newAttempt.set(false);

        // If verification unsuccessful, expire the attempt
        if (!adminKeyVerified.get() && !regularKeyVerified.get()) {
            cardHintText.set("Please Scan Card ...");
        }
    }
}
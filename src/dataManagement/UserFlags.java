package dataManagement;

/**
 * Data type holds information on user admin and vote status as well as their
 * initialization vector used to decrypt user strings from file.
 * 
 * @author Aidan
 *
 */
public class UserFlags {

    private final String iv;
    private boolean admin;
    private boolean voted;

    public UserFlags(String iv, boolean admin, boolean voted) {
        this.iv = iv;
        this.admin = admin;
        this.voted = voted;
    }

    public String getIV() {
        return iv;
    }

    public void setAdmin(boolean value) {
        admin = value;
    }

    public void setVoted(boolean value) {
        voted = value;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean hasVoted() {
        return voted;
    }
}

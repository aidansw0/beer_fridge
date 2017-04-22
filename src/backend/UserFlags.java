package backend;

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

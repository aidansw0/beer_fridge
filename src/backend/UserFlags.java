package backend;

public class UserFlags {
    
    private boolean admin;
    private boolean voted;
    
    public UserFlags(boolean admin, boolean voted) {
        this.admin = admin;
        this.voted = voted;
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

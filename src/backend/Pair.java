package backend;

public class Pair<Key, Val> {
    
    private final Key key;
    private final Val val;
    
    public Pair(Key key, Val val) {
        this.key = key;
        this.val = val;
    }
    
    public Key getKey() {
        return key;
    }
    
    public Val getVal() {
        return val;
    }

}

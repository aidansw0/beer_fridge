package gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ButtonManager {
    
    private final Map<String, Integer> beerTypeLikes = new HashMap<String, Integer>();
    private final List<String> beerTypes = new ArrayList<String>();

    private void setButtons() {
        
        beerTypeLikes.put("beer0", 0);
        beerTypeLikes.put("beer1", 0);
        beerTypeLikes.put("beer2", 0);
        beerTypeLikes.put("beer3", 0);
        
        
        beerTypes.add("beer0");
        beerTypes.add("beer1");
        beerTypes.add("beer2");
        beerTypes.add("beer3");
        
    }

}

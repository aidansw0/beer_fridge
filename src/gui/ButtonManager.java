package gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

import javax.swing.text.html.ImageView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ButtonManager {
    
    private Map<String, Integer> beerTypeLikes = new HashMap<String, Integer>();
    private List<String> beerTypes = new ArrayList<String>();
    private int currentBeer = 0;

    public ButtonManager() {
        beerTypeLikes.put("beer0", 0);
        beerTypeLikes.put("beer1", 0);
        beerTypeLikes.put("beer2", 0);
        beerTypeLikes.put("beer3", 0);
        
        beerTypes.add("beer0");
        beerTypes.add("beer1");
        beerTypes.add("beer2");
        beerTypes.add("beer3");
    }

    public Button createLeftButton(Text display) {
        Button left = new Button("");
        left.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (currentBeer > 0) {
                    currentBeer--;
                } else if (currentBeer == 0) {
                    currentBeer = beerTypes.size() - 1;
                }

                display.setText(beerTypes.get(currentBeer));
            }
        });
        return left;
    }

    public Button createRightButton(Text display) {
        Button right = new Button("");
        right.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                if (currentBeer < beerTypes.size() - 1) {
                    currentBeer++;
                } else if (currentBeer == beerTypes.size() - 1) {
                    currentBeer = 0;
                }

                display.setText(beerTypes.get(currentBeer));
            }
        });
        return right;
    }

    public String getCurrent() {
        return beerTypes.get(currentBeer);
    }
}

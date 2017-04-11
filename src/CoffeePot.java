import io.dweet.Dweet;
import io.dweet.DweetIO;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

class CoffeePot {
    private static final Duration REFRESH_FREQUENCY = Duration.seconds(2);

    private final ReadOnlyIntegerWrapper weight;
    private final ReadOnlyIntegerWrapper temperature;
    private final GetData probe;
    private final Timeline timeline;

    public ReadOnlyIntegerProperty weightProperty() {
        return weight.getReadOnlyProperty();
    }
    public ReadOnlyIntegerProperty tempProperty() {
        return temperature.getReadOnlyProperty();
    }

    public CoffeePot() {
        probe       = new GetData();
        weight      = new ReadOnlyIntegerWrapper(probe.readWeight());
        temperature = new ReadOnlyIntegerWrapper(probe.readTemp());

        timeline = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new EventHandler<ActionEvent>() {
                            @Override public void handle(ActionEvent actionEvent) {
                                probe.getLatest();
                                weight.set(probe.readWeight());
                                temperature.set(probe.readTemp());
                            }
                        }
                ),
                new KeyFrame(
                        REFRESH_FREQUENCY
                )
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
}

class GetData {
    private Dweet dweet;
    private float percentage;
    private int rectangleHeight;

    public void getLatest() {
        try {
            dweet = DweetIO.getLatestDweet("richardcoffeepot");
        } catch (Exception e) { }
    }

    public int readWeight() {
        try {
            percentage = dweet.getContent().get("weight").getAsFloat() / 1750 * 100;

            if (percentage > 100) {
                percentage = 100;
            } else if (percentage < 0) {
                percentage = 0;
            }

            // Range in pixels is between empty and full pot is 120px and 350px out of 600px
            // Translates to between 20% to 58%
            rectangleHeight = (int) (percentage * 0.38 + 20);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rectangleHeight;
    }

    public int readTemp() {
        try {
            percentage = (float) (dweet.getContent().get("temp").getAsFloat() / 83.5 * 100);

            if (percentage > 100) {
                percentage = 100;
            } else if (percentage < 0) {
                percentage = 0;
            }

            // Range in pixels is between empty and full pot is 150px and 460px out of 600px
            // Translates to between 25% to 77%
            rectangleHeight = (int) (percentage * 0.52 + 25);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rectangleHeight;
    }
}
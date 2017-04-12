package backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * This class is responsible for reading and writing data to and from the local
 * file system. All data is stored in JSON format.
 * 
 * @author Aidan
 *
 */
public class SaveData {

    private final String JSON_FILE = "beers_json_test.json"; // file name, NOT
                                                             // file location
    private final String fullJSONFilePath;
    // directory for JSON_FILE to be stored, same as location as the .jar

    private final Map<String, Integer> beerRatings;
    private final List<String> beers;

    public SaveData(Map<String, Integer> map, List<String> list) {
        beerRatings = map;
        beers = list;
        fullJSONFilePath = getJarPath() + "data" + System.getProperty("file.separator") + JSON_FILE;

        if (!checkFileExists()) {
            createJSONDirectory();
            writeJSON();
        } else {
            readJSON();
        }
    }

    /**
     * This method updates JSON_FILE with the most recent data stored in
     * beerRatings. JSON_FILE is completely overwritten during the process.
     */
    public void writeJSON() {
        if (beerRatings.keySet().size() > 0) {
            JSONObject jsonToWrite = new JSONObject();
            JSONArray jsonBeerList = new JSONArray();

            try {
                for (String beerName : beerRatings.keySet()) {
                    JSONObject jsonBeer = new JSONObject();
                    jsonBeer.put(beerName, beerRatings.get(beerName));
                    jsonBeerList.put(jsonBeer);
                }

                jsonToWrite.put("beers", jsonBeerList);
                FileWriter writer = new FileWriter(fullJSONFilePath);
                System.out.println(fullJSONFilePath);
                writer.write(jsonToWrite.toString());
                writer.close();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

        } else {
            try {
                // if beerRatings is empty then either readJSON() couldn't read
                // the data file or the file was deleted, in either case a new
                // empty file is created
                FileWriter writer = new FileWriter(fullJSONFilePath);
                writer.write("");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reads and parses JSON_FILE and loads the data into beerRatings and beers.
     * Does not modify JSON_FILE in any way.
     */
    private void readJSON() {
        StringBuilder jsonText = new StringBuilder();
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(fullJSONFilePath));
            String line = reader.readLine();

            while (line != null) {
                jsonText.append(line);
                line = reader.readLine();
            }

            JSONObject obj = (JSONObject) new JSONTokener(jsonText.toString()).nextValue();
            JSONArray jsonBeers = obj.getJSONArray("beers");

            for (int i = 0; i < jsonBeers.length(); i++) {
                JSONObject nextBeer = jsonBeers.getJSONObject(i);
                String beerName = nextBeer.keys().next().toString();

                beerRatings.put(beerName, nextBeer.getInt(beerName));
                beers.add(beerName);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            createJSONDirectory();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new directory for the JSON_FILE to be stored in. The new
     * directory will be in the same location as the .jar file for this
     * application.
     */
    private void createJSONDirectory() {
        String newDir = "";
        String path = getJarPath();

        String fileSeparator = System.getProperty("file.separator");
        newDir = path + "data" + fileSeparator;
        JOptionPane.showMessageDialog(null, newDir);

        File file = new File(newDir);
        file.mkdir();
    }

    /**
     * Locates the directory in which the .jar executable file for this
     * application exits.
     * 
     * @return String giving the full path of the .jar executable
     */
    private String getJarPath() {
        URL url = SaveData.class.getProtectionDomain().getCodeSource().getLocation();
        String jarPath = "";
        try {
            jarPath = URLDecoder.decode(url.getFile(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String parentPath = new File(jarPath).getParentFile().getPath();
        return parentPath + System.getProperty("file.separator");
    }

    /**
     * Checks weather fullJSONFilePath exists.
     * 
     * @return true if fullJSONFilePath exists and false if otherwise.
     */
    private boolean checkFileExists() {
        File testFile = null;
        testFile = new File(fullJSONFilePath);
        return testFile.exists();
    }
}

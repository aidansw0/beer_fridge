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

public class SaveData {

    private final String JSON_FILE = "beers_json_test.json";
    private final String fullJSONFilePath; // directory where JSON_FILE should
                                           // be
    // stored
    // (same as .jar file)

    private final Map<String, Integer> beerRatings;
    private final List<String> beers;

    public SaveData(Map<String, Integer> map, List<String> list) {
        beerRatings = map;
        beers = list;
        fullJSONFilePath = getJarPath() + "\"" + JSON_FILE;

        if (!checkFileExists()) {
            createJSONDirectory();
            writeJSON();
        } else {

        }

    }

    private void writeJSON() {
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
                writer.write(jsonToWrite.toString());
                writer.close();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        } else {
            FileWriter writer;
            try {
                writer = new FileWriter(fullJSONFilePath);
                writer.write("");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readJSON() {
        StringBuilder jsonText = new StringBuilder();
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(JSON_FILE));
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

    private String createJSONDirectory() {
        String newDir = "";

        String path = getJarPath();

        String fileSeparator = System.getProperty("file.separator");
        newDir = path + fileSeparator + "data" + fileSeparator;
        JOptionPane.showMessageDialog(null, newDir);

        File file = new File(newDir);
        file.mkdir();

        return newDir;
    }

    private String getJarPath() {
        URL url = SaveData.class.getProtectionDomain().getCodeSource().getLocation();
        String jarPath = "";
        try {
            jarPath = URLDecoder.decode(url.getFile(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String parentPath = new File(jarPath).getParentFile().getPath();
        System.out.println(parentPath);
        return parentPath;
    }

    private boolean checkFileExists() {
        File testFile = null;
        testFile = new File(fullJSONFilePath);
        System.out.println(testFile.exists());
        return testFile.exists();
    }
}

package backend;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * This class is responsible for reading and writing data to and from the local
 * file system. All data is stored in JSON format. This class should be
 * instantiated only once at the beginning of the program execution at which
 * point it will look for data files and load their contents into memory. If the
 * data files cannot be found then empty files will be created. From this point
 * new data can be written whenever needed.
 * 
 * @author Aidan
 *
 */
public class SaveData {

    private final String JSON_FILE = "beers_json_test.json"; // file name, NOT
                                                             // file location
    private final String RFID_FILE = "dont_open.dat";

    private final String fullJSONFilePath;
    private final String fullRFIDFilePath;
    // full paths for JSON_FILE and RFID_FILE to be stored (includes file name),
    // dependent on location of .jar file

    private final String KEY = "DES"; // Encryption key for RFID file

    private final Map<String, Integer> beerRatings;
    private final List<String> beers;
    private boolean dataReady = false;

    public SaveData(Map<String, Integer> map, List<String> list) {
        beerRatings = map;
        beers = list;
        fullJSONFilePath = getJarPath() + "data" + System.getProperty("file.separator") + JSON_FILE;
        fullRFIDFilePath = getJarPath() + "data" + System.getProperty("file.separator") + RFID_FILE;

        if (!checkFileExists(fullJSONFilePath)) {
            createFileInDataDirectory(JSON_FILE);
            try {
                writeData();
                dataReady = true;
            } catch (JSONException | IOException e) {
                e.printStackTrace();
                dataReady = false;
            }
        } else {
            readJSONData();
            dataReady = true;
        }
    }

    /**
     * @return true if data has been loaded into memory and false if otherwise.
     */
    public boolean isDataReady() {
        return dataReady;
    }

    /**
     * Encrypts and appends rfid to RFID_FILE, does not overwrite RFID_FILE. If
     * RFID_FILE cannot be located then a new file will be created and written
     * to instead.
     * 
     * @param rfid,
     *            String giving the RFID code to be written.
     * @throws IOException
     *             if there is a problem locating the RFID file.
     */
    public void addRFID(String rfid) throws IOException {
        if (checkFileExists(RFID_FILE)) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fullRFIDFilePath, true));
            writer.write(encrypt(rfid.getBytes()));
            writer.newLine();
            writer.close();

        } else {
            createFileInDataDirectory(RFID_FILE);

            BufferedWriter writer = new BufferedWriter(new FileWriter(fullRFIDFilePath, true));
            writer.write(encrypt(rfid.getBytes()));
            writer.newLine();
            writer.close();
        }
    }

    /**
     * Checks weather the given rfid string exists in RFID_FILE. If RFID_FILE
     * cannot be located then a new file will be created.
     * 
     * @param rfid,
     *            String to search for in RFID_FILE
     * @return true if rfid exists in RFID_FILE and false if otherwise.
     */
    public boolean checkRFID(String rfid) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fullRFIDFilePath));
            String line = reader.readLine();

            while (line != null) {
                if (rfid.equals(line)) {
                    return true;
                }
                line = reader.readLine();
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
            createFileInDataDirectory(RFID_FILE);
            return false;
        }
        return false;
    }

    /**
     * Encrypts text using KEY as an encryption key.
     * 
     * @param text,
     *            byte[] to be encrypted
     * @return String representation of the encrypted byte[]
     */
    private String encrypt(byte[] text) {
        byte[] textEncrypted = null;

        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(KEY);
            SecretKey key = keyGen.generateKey();

            Cipher cipher = Cipher.getInstance(KEY);
            cipher.init(Cipher.ENCRYPT_MODE, key); // set cipher to encrypt mode

            textEncrypted = cipher.doFinal(text);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            e.printStackTrace();
        }

        return new String(textEncrypted);
    }

    /**
     * This method updates JSON_FILE with the most recent data stored in
     * beerRatings. JSON_FILE is completely overwritten during the process. If
     * the JSON file cannot be found then a new file is created in the data
     * directory.
     * 
     * @throws JSONException
     *             if data could not be written in JSON format.
     * @throws IOException
     *             if data could not be written.
     */
    public void writeData() throws JSONException, IOException {
        if (checkFileExists(JSON_FILE)) {
            JSONObject jsonToWrite = new JSONObject();
            JSONArray jsonBeerList = new JSONArray();

            for (String beerName : beerRatings.keySet()) {
                JSONObject jsonBeer = new JSONObject();
                jsonBeer.put(beerName, beerRatings.get(beerName));
                jsonBeerList.put(jsonBeer);
            }

            jsonToWrite.put("beers", jsonBeerList);
            FileWriter writer = new FileWriter(fullJSONFilePath);
            writer.write(jsonToWrite.toString());
            writer.close();

        } else {
            createFileInDataDirectory(JSON_FILE);

            JSONObject jsonToWrite = new JSONObject();
            JSONArray jsonBeerList = new JSONArray();

            for (String beerName : beerRatings.keySet()) {
                JSONObject jsonBeer = new JSONObject();
                jsonBeer.put(beerName, beerRatings.get(beerName));
                jsonBeerList.put(jsonBeer);
            }

            jsonToWrite.put("beers", jsonBeerList);
            FileWriter writer = new FileWriter(fullJSONFilePath);
            writer.write(jsonToWrite.toString());
            writer.close();
        }
    }

    /**
     * Reads and parses JSON_FILE and loads the data into beerRatings and beers.
     * Does not modify JSON_FILE in any way. If the JSON file cannot be located
     * a new file is created.
     */
    private void readJSONData() {
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
            createFileInDataDirectory(JSON_FILE);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new directory for the JSON_FILE to be stored in. The new
     * directory will be in the same location as the .jar file for this
     * application.
     */
    private void createFileInDataDirectory(String fileName) {
        String newDir = "";
        String path = getJarPath();

        String fileSeparator = System.getProperty("file.separator");
        newDir = path + "data" + fileSeparator;
        // JOptionPane.showMessageDialog(null, newDir);

        File file = new File(newDir + fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Locates the directory in which the .jar executable file for this
     * application exits.
     * 
     * @return String giving the full path of the .jar executable including a
     *         system dependent file separator at the end of the path.
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
     * Checks weather a file at fullPath exists
     * 
     * @return true if a file at fullPath exists and false if otherwise.
     */
    private boolean checkFileExists(String fullPath) {
        File testFile = null;
        testFile = new File(fullPath);
        return testFile.exists();
    }
}

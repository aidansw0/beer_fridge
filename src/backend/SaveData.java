package backend;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

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

    private final String JSON_FILE = "beer_data.json";
    private final String RFID_FILE = "user_data.json";
    private final String SALT_FILE = "hac.bin"; // stores salt for encryption

    private final String fullSALTPath;
    private final String fullJSONFilePath;
    private final String fullRFIDFilePath;
    // full paths for JSON_FILE and RFID_FILE to be stored (includes file name),
    // dependent on location of .jar file

    private final Map<String, Integer> beerRatings;
    private final List<String> beers;
    private boolean beerDataReady = false;

    private final int KEY_DERIVATION_ITERATION = 65536;
    private final int KEY_SIZE = 128;

    private final char[] PASSWORD = { 'a', 'b' };
    private byte[] SALT;// = new byte[KEY_SIZE];
    private Cipher cipher;

    private final Map<String, UserFlags> userData = new HashMap<String, UserFlags>();
    private boolean userDataReady = false;

    public SaveData(Map<String, Integer> map, List<String> list) {
        fullSALTPath = getJarPath() + "data" + System.getProperty("file.separator") + SALT_FILE;

        if (!checkFileExists(fullSALTPath)) {
            writeSALT();
        } else {
            readSALT();
        }

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(PASSWORD, SALT, KEY_DERIVATION_ITERATION, KEY_SIZE);
            SecretKey tmpKey = factory.generateSecret(spec);
            SecretKey secretKey = new SecretKeySpec(tmpKey.getEncoded(), "AES");

            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        } catch (Exception e) {
            e.printStackTrace();
        }

        beerRatings = map;
        beers = list;
        fullJSONFilePath = getJarPath() + "data" + System.getProperty("file.separator") + JSON_FILE;
        fullRFIDFilePath = getJarPath() + "data" + System.getProperty("file.separator") + RFID_FILE;

        if (!checkFileExists(fullJSONFilePath)) {
            createFileInDataDirectory(JSON_FILE);
            try {
                writeData();
            } catch (JSONException | IOException e) {
                e.printStackTrace();
                beerDataReady = false;
            }
        } else {
            readJSONData();
        }
        
        if (!checkFileExists(fullRFIDFilePath)) {
            createFileInDataDirectory(RFID_FILE); 
            try {
                writeBufferToFile();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                userDataReady = false;
            }
        } else {
            loadIntoBuffer();
        }
       
    }

    /**
     * @return true if data has been loaded into memory and false if an error an
     *         occured or if no data existed during the last call to
     *         readData()/writeData(). This may happen if either: beerRatings
     *         contained no data to write to JSON_FILE or if JSON_FILE did not
     *         contain any data to read.
     */
    public boolean isBeerDataReady() {
        return beerDataReady;
    }

    public boolean isUserDataReady() {
        return userDataReady;
    }

    public void addUserToBuffer(String rfid) {
        if (!checkUserInBuffer(rfid)) {
            userData.put(encrypt(rfid), new UserFlags(false, false));
        }
    }

    public boolean checkUserInBuffer(String rfid) {
        return userData.keySet().contains(encrypt(rfid));
    }

    public boolean checkAdmin(String rfid) {
        return userData.get(encrypt(rfid)).isAdmin();
    }

    public boolean checkVoted(String rfid) {
        return userData.get(encrypt(rfid)).hasVoted();
    }

    public void writeBufferToFile() throws IOException, JSONException {
        if (checkFileExists(RFID_FILE)) {

            if (userData.keySet().size() > 0) {
                JSONObject jsonFile = new JSONObject();
                JSONArray dataArray = new JSONArray();

                for (String userID : userData.keySet()) {
                    JSONObject user = new JSONObject();
                    user.put("id", userID);
                    user.put("admin", userData.get(userID).isAdmin());
                    user.put("voted", userData.get(userID).hasVoted());
                    dataArray.put(user);
                }

                jsonFile.put("users", dataArray);
                FileWriter writer = new FileWriter(fullRFIDFilePath);
                writer.write(jsonFile.toString());
                writer.flush();
                writer.close();

                userDataReady = true;
            } else {
                userDataReady = false;
            }

        } else {
            createFileInDataDirectory(RFID_FILE);

            if (userData.keySet().size() > 0) {
                JSONObject jsonFile = new JSONObject();
                JSONArray dataArray = new JSONArray();

                for (String userID : userData.keySet()) {
                    JSONObject user = new JSONObject();
                    user.put("id", userID);
                    user.put("admin", userData.get(userID).isAdmin());
                    user.put("voted", userData.get(userID).hasVoted());
                    dataArray.put(user);
                }

                jsonFile.put("users", dataArray);
                FileWriter writer = new FileWriter(fullRFIDFilePath);
                writer.write(jsonFile.toString());
                writer.flush();
                writer.close();

                userDataReady = true;
            } else {
                userDataReady = false;
            }
        }
    }

    public void loadIntoBuffer() {
        
        try {
            String jsonString = readFileToString(fullRFIDFilePath);
            if (jsonString == null) {
                userDataReady = false;
                return;
            }
            
            userDataReady = false;
            
            JSONObject obj = (JSONObject) new JSONTokener(jsonString).nextValue();
            JSONArray users = obj.getJSONArray("users");
            
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                boolean admin = user.getBoolean("admin");
                boolean voted = user.getBoolean("voted");
                userData.put(user.getString("id"), new UserFlags(admin, voted));
            }
            
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private String encrypt(String plainText) {
        byte[] ciphertext = null;

        try {
            ciphertext = cipher.doFinal(plainText.getBytes("UTF-8"));

        } catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] encryptedBytes = Base64.getEncoder().encode(ciphertext);
        //System.out.println(new String(encryptedBytes));
        return new String(encryptedBytes);

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

            if (beerRatings.keySet().size() > 0) {
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
                writer.flush();
                writer.close();

                beerDataReady = true;
            } else {
                beerDataReady = false;
            }

        } else {
            createFileInDataDirectory(JSON_FILE);

            if (beerRatings.keySet().size() > 0) {
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

                beerDataReady = true;
            } else {
                beerDataReady = false;
            }

        }
    }

    /**
     * Reads and parses JSON_FILE and loads the data into beerRatings and beers.
     * Does not modify JSON_FILE in any way. If the JSON file cannot be located
     * a new file is created.
     */
    private void readJSONData() {

        try {
            String jsonString = readFileToString(fullJSONFilePath);
            if (jsonString == null) {
                beerDataReady = false;
                return;
            }
            
            beerDataReady = false;
            
            JSONObject obj = (JSONObject) new JSONTokener(jsonString).nextValue();
            JSONArray jsonBeers = obj.getJSONArray("beers");

            for (int i = 0; i < jsonBeers.length(); i++) {
                JSONObject nextBeer = jsonBeers.getJSONObject(i);
                String beerName = nextBeer.keys().next().toString();

                beerRatings.put(beerName, nextBeer.getInt(beerName));
                beers.add(beerName);
                beerDataReady = true;
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            beerDataReady = false;
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
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            System.out.println(file.getAbsolutePath());
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

    private String readFileToString(String filePath) throws IOException {
        StringBuilder fileText = new StringBuilder();
        BufferedReader reader;

            reader = new BufferedReader(new FileReader(filePath));
            String line = reader.readLine();

            if (line == null) {
                reader.close();
                return null;
            }

            while (line != null) {
                fileText.append(line);
                line = reader.readLine();
            }
            
            reader.close();

        return fileText.toString();
    }

    private void writeSALT() {
        createFileInDataDirectory(SALT_FILE);
        SecureRandom secureRandom = new SecureRandom();
        SALT = secureRandom.generateSeed(8);
//        try {
//            System.out.println("write: " + new String(SALT, "UTF-8"));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

        try {
            FileOutputStream fos = new FileOutputStream(fullSALTPath);
            fos.write(SALT);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readSALT() {
        File saltFile = new File(fullSALTPath);
        SALT = new byte[(int) saltFile.length()];

        try {
            FileInputStream fis = new FileInputStream(fullSALTPath);
            fis.read(SALT);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        try {
//            System.out.println("read: " + new String(SALT, "UTF-8"));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
    }
}

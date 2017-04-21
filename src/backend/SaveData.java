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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
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

    private final String JSON_FILE = "beers_json_test.json"; // file name, NOT
                                                             // file location
    private final String RFID_FILE = "dont_open.dat";
    private final String SALT_FILE = "salt.bin";

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
    private byte[] SALT = new byte[KEY_SIZE];
    private Cipher cipher;
    

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
        
        try {
            addRFID("adsad");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
            System.out.println("file exists");
            BufferedWriter writer = new BufferedWriter(new FileWriter(fullRFIDFilePath, true));
            Pair<byte[], byte[]> encrypted = encrypt(rfid);
            
            try {
                writer.write(encrypted.getKey().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            writer.newLine();
            writer.close();

        } else {
            createFileInDataDirectory(RFID_FILE);

            BufferedWriter writer = new BufferedWriter(new FileWriter(fullRFIDFilePath, true));
            Pair<byte[], byte[]> encrypted = encrypt(rfid);
            
            try {
                writer.write(encrypted.getKey().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                System.out.println(line);
                // if (rfid.equals(convertString(line, false))) {
                // return true;
                // }
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

    private Pair<byte[], byte[]> encrypt(String plainText) {

        byte[] iv = null;
        byte[] ciphertext = null;
        
        Pair<byte[], byte[]> encrypted = null;

        try {
            AlgorithmParameters params = cipher.getParameters();
            iv = params.getParameterSpec(IvParameterSpec.class).getIV();
            ciphertext = cipher.doFinal(plainText.getBytes("UTF-8"));
            encrypted = new Pair<byte[], byte[]>(ciphertext, iv);
            
        } catch (InvalidParameterSpecException | IllegalBlockSizeException | BadPaddingException
                | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println("rfid: " + ciphertext.toString());

        return encrypted;

    }

    private byte[] decrypte(byte[] bytes) {
        

        return null;
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
        StringBuilder jsonText = new StringBuilder();
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(fullJSONFilePath));
            String line = reader.readLine();

            if (line == null) {
                beerDataReady = false;
                reader.close();
                return;
            }

            while (line != null) {
                jsonText.append(line);
                line = reader.readLine();
            }

            beerDataReady = false;

            JSONObject obj = (JSONObject) new JSONTokener(jsonText.toString()).nextValue();
            JSONArray jsonBeers = obj.getJSONArray("beers");

            for (int i = 0; i < jsonBeers.length(); i++) {
                JSONObject nextBeer = jsonBeers.getJSONObject(i);
                String beerName = nextBeer.keys().next().toString();

                beerRatings.put(beerName, nextBeer.getInt(beerName));
                beers.add(beerName);
                beerDataReady = true;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            createFileInDataDirectory(JSON_FILE);
            beerDataReady = false;
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

    private void writeSALT() {
        createFileInDataDirectory(SALT_FILE);
        SecureRandom secureRandom = new SecureRandom();
        SALT = secureRandom.generateSeed(8);
        System.out.println("write: " + SALT);

        try {
            FileOutputStream fos = new FileOutputStream(fullSALTPath);
            fos.write(SALT);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readSALT() {
        try {
            FileInputStream fis = new FileInputStream(fullSALTPath);
            fis.read(SALT);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("read: " + SALT);
    }
}

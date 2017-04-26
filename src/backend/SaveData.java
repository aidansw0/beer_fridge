package backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import tools.Util;

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

    private static final String BEER_FILE = "beer_data.json";
    private static final String USER_FILE = "user_data.json";
    private static final String SALT_FILE = "hac.bin"; // stores salt for
                                                       // encryption

    private final String fullSaltPath;
    private final String fullBeerFilePath;
    private final String fullUserFilePath;
    // full paths for SALT_FILE, BEER_FILE and USER_FILE to be stored (includes
    // file name), dependent on location of .jar file

    private boolean beerDataReady = false;

    // constants for encryption
    private static final int KEY_DERIVATION_ITERATION = 65536;
    private static final int KEY_SIZE = 128;
    private static final char[] PASSWORD = { 'z', 'e', 'r', 'o', 'c', 'l', 'i', 'e', 'n', 't' };
    private static byte[] SALT;
    private Cipher cipher;

    private final Map<String, UserFlags> userData = new HashMap<String, UserFlags>();
    private boolean userDataReady = false;

    /**
     * Assigns path names for data files and loads beer and user information
     * into memory if available.
     */
    public SaveData() {
        fullSaltPath = Util.getJarPath() + "data" + System.getProperty("file.separator") + SALT_FILE;
        fullBeerFilePath = Util.getJarPath() + "data" + System.getProperty("file.separator") + BEER_FILE;
        fullUserFilePath = Util.getJarPath() + "data" + System.getProperty("file.separator") + USER_FILE;

        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e1) {
            e1.printStackTrace();
        }

        // check SALT_FILE
        if (!Util.checkFileExists(fullSaltPath)) {
            writeSALT();
        } else {
            readSALT();
        }

        // check BEER_FILE
        if (!Util.checkFileExists(fullBeerFilePath)) {
            createFileInDataDirectory(BEER_FILE);
            beerDataReady = false;
        } else {
            readBeerData();
        }

        // check USER_FILE
        if (!Util.checkFileExists(fullUserFilePath)) {
            createFileInDataDirectory(USER_FILE);
            userDataReady = false;
        } else {
            readUsersFromFile();
        }
    }

    /**
     * Sets the cipher into encryption mode. Once set the cipher may be used to
     * encrypt as many items as needed.
     */
    private void setCipherEncrypt() {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(PASSWORD, SALT, KEY_DERIVATION_ITERATION, KEY_SIZE);
            SecretKey tmpKey = factory.generateSecret(spec);
            SecretKey secretKey = new SecretKeySpec(tmpKey.getEncoded(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        } catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the cipher to decryption mode. Once set the cipher may only be used
     * to decrpyt the one item that corresponds the the initialization vector,
     * iv. The initialization vector is generated upon encryption of the item
     * and is required to decrypt that same item.
     * 
     * @param iv
     *            byte[] giving the initialization vector generated upon
     *            encryption of its corresponding item.
     */
    private void setCipherDecrypt(byte[] iv) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(PASSWORD, SALT, KEY_DERIVATION_ITERATION, KEY_SIZE);
            SecretKey tmpKey = factory.generateSecret(spec);
            SecretKey secretKey = new SecretKeySpec(tmpKey.getEncoded(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

        } catch (InvalidKeyException | InvalidAlgorithmParameterException | InvalidKeySpecException
                | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return true if data has been loaded into memory and false if an error an
     *         occurred or if no data was transferred during the last call to
     *         readBeerData()/writeBeerData(). This may happen if either:
     *         beerRatings contained no data to write to BEER_FILE or if
     *         BEER_FILE did not contain any data to read.
     */
    public boolean isBeerDataReady() {
        return beerDataReady;
    }

    /**
     * @return true if data was successfully loaded into memory and false if an
     *         error occurred or if no data was transferred during the last call
     *         to writeUsersToFile()/readUsersFromFile(). This may happen if
     *         either: userData did not contain any data to write to USER_FILE
     *         or USER_FILE did not contain any data to read.
     */
    public boolean isUserDataReady() {
        return userDataReady;
    }

    /**
     * Adds the given user identified by user to a buffer stored in RAM. This
     * buffer should also contain user data read from USER_FILE. If the user
     * already exists then the user is not added to the buffer.
     * 
     * @param user
     *            String to identify the user. Must not be the empty string.
     * @return true if the user did not already exist and was successfully added
     *         to the buffer and false if otherwise.
     */
    public boolean addUser(String user) {
        if (!checkUserExists(user)) {
            userData.put(user, new UserFlags("", false, false));
            // add temporary empty string for user's IV. this will be generated
            // when the user string is encrypted and written to file
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if the given user exists in the buffer stored in RAM.
     * 
     * @param user
     *            String giving the user to be identified.
     * @return true if the user exists and false if otherwise.
     */
    public boolean checkUserExists(String user) {
        return userData.keySet().contains(user);
    }

    /**
     * Checks if the given user is flagged with administrative privileges.
     * 
     * @param user
     *            String giving the user to be identified.
     * @return true if the user exists and has administrative privileges and
     *         false if otherwise or the user does not exist.
     */
    public boolean checkAdmin(String user) {
        if (checkUserExists(user)) {
            return userData.get(user).isAdmin();
        } else {
            return false;
        }
    }

    /**
     * Checks if the given user is flagged as having already voted.
     * 
     * @param user
     *            String giving the user to be identified.
     * @return true if the user exists and has already voted and false if
     *         otherwise or if the user has not voted.
     */
    public boolean checkVoted(String user) {
        if (checkUserExists(user)) {
            return userData.get(user).hasVoted();
        } else {
            return false;
        }
    }

    /**
     * Sets the given user's administrative privileges to 'value.' If the user
     * did not already exist then a new user is added with admin set to 'value'
     * and voted set to false.
     * 
     * @param user
     *            String giving user to be identified.
     * @param value
     *            boolean giving value to set as administrative privileges.
     */
    public void setAdmin(String user, boolean value) {
        if (checkUserExists(user)) {
            userData.get(user).setAdmin(value);
        } else {
            userData.put(user, new UserFlags("", value, false));
        }
    }

    /**
     * Sets the given user's voted status to 'value.' If the user did not
     * already exist then a new user is added with voted set to 'value' and
     * admin set to false.
     * 
     * @param user
     *            String giving user to be identified.
     * @param value
     *            boolean giving value to set as user's voted status.
     */
    public void setVoted(String user, boolean value) {
        if (checkUserExists(user)) {
            userData.get(user).setVoted(value);
        } else {
            userData.put(user, new UserFlags("", false, value));
        }
    }

    /**
     * Resets all users voted status to false.
     */
    public void resetVotes() {
        for (String user : userData.keySet()) {
            setVoted(user, false);
        }
    }

    /**
     * Overwrites USER_FILE with any user data that is contained in the user
     * buffer, userData. If USER_FILE does not exist then a new file is created
     * and written to. All user identification strings are encrypted before
     * being written to the file.
     * 
     * @throws IOException
     * @throws JSONException
     */
    public synchronized void writeUsersToFile() throws JSONException, IOException {
        setCipherEncrypt();

        if (Util.checkFileExists(fullUserFilePath)) {

            if (userData.keySet().size() > 0) {
                JSONObject jsonFile = new JSONObject();
                JSONArray dataArray = new JSONArray();

                for (String userID : userData.keySet()) {
                    JSONObject user = new JSONObject();
                    String[] encrypted = encrypt(userID);
                    user.put("id", encrypted[0]);
                    user.put("iv", encrypted[1]);
                    user.put("admin", userData.get(userID).isAdmin());
                    user.put("voted", userData.get(userID).hasVoted());
                    dataArray.put(user);
                }

                jsonFile.put("users", dataArray);
                FileWriter writer = new FileWriter(fullUserFilePath, false);
                writer.write(jsonFile.toString());
                writer.flush();
                writer.close();

                userDataReady = true;
            } else {
                File file = new File(fullUserFilePath);
                file.delete();
                userDataReady = false;
            }

        } else {
            createFileInDataDirectory(USER_FILE);

            if (userData.keySet().size() > 0) {
                JSONObject jsonFile = new JSONObject();
                JSONArray dataArray = new JSONArray();

                for (String userID : userData.keySet()) {
                    JSONObject user = new JSONObject();
                    String[] encrypted = encrypt(userID);
                    user.put("id", encrypted[0]);
                    user.put("iv", encrypted[1]);
                    user.put("admin", userData.get(userID).isAdmin());
                    user.put("voted", userData.get(userID).hasVoted());
                    dataArray.put(user);
                }

                jsonFile.put("users", dataArray);
                FileWriter writer = new FileWriter(fullUserFilePath);
                writer.write(jsonFile.toString());
                writer.flush();
                writer.close();

                userDataReady = true;
            } else {
                userDataReady = false;
            }
        }
    }

    /**
     * Reads user data from USER_FILE into the user buffer, userData. Since user
     * strings contained in USER_FILE are encrypted they are decrpyted before
     * being loaded into memory.
     */
    private void readUsersFromFile() {

        try {
            String jsonString = Util.readFileToString(fullUserFilePath);
            if (jsonString == null) {
                userDataReady = false;
                return;
            }

            userDataReady = false;

            JSONObject obj = (JSONObject) new JSONTokener(jsonString).nextValue();
            JSONArray users = obj.getJSONArray("users");

            for (int i = 0; i < users.length(); i++) {
                userDataReady = true;
                JSONObject user = users.getJSONObject(i);
                boolean admin = user.getBoolean("admin");
                boolean voted = user.getBoolean("voted");

                String id = decrypt(user.getString("id"), user.getString("iv"));

                userData.put(id, new UserFlags(user.getString("iv"), admin, voted));
            }

        } catch (IOException | JSONException e) {
            userDataReady = false;
            e.printStackTrace();
        }
    }

    /**
     * Encrypts the given string into a byte[] which is then encoded using
     * base64 into string format for easier storage. During encryption an
     * initialization vector, a byte[], is generated. This byte[] is unique to
     * the encrypted string and is required upon the strings decrpytion. The
     * initialization vector is also encoded into string format using base64
     * before being returned.
     * 
     * @param plainText
     *            String to be encrypted. Must not be the empty string.
     * @return String[] of length 2 containing the encrypted plainText at index
     *         0 and its corresponding initialization vector at index 1.
     */
    private String[] encrypt(String plainText) {
        byte[] ciphertext = null;
        byte[] iv = null;
        String[] encrypted = new String[2];

        try {
            AlgorithmParameters params = cipher.getParameters();
            iv = params.getParameterSpec(IvParameterSpec.class).getIV();
            ciphertext = cipher.doFinal(plainText.getBytes("UTF-8"));

        } catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException
                | InvalidParameterSpecException e) {
            e.printStackTrace();
        }

        String cipherString = Base64.getEncoder().encodeToString(ciphertext);
        String ivString = Base64.getEncoder().encodeToString(iv);

        encrypted[0] = cipherString;
        encrypted[1] = ivString;

        return encrypted;
    }

    /**
     * Decrypts the given string using the provided initialization vector.
     * 
     * @param encrypted
     *            String to be decrypted. Musy not be the empty string.
     * @param iv
     *            String giving the initialization vector to be used during the
     *            decryption process.
     * @return the decrypted String of encrypted.
     */
    private String decrypt(String encrypted, String iv) {
        byte[] ivBytes = null;
        byte[] encryptedBytes = null;
        String decrypted = null;

        try {
            ivBytes = Base64.getDecoder().decode(iv.getBytes("UTF-8"));
            encryptedBytes = Base64.getDecoder().decode(encrypted.getBytes("UTF-8"));
            setCipherDecrypt(ivBytes);
            decrypted = new String(cipher.doFinal(encryptedBytes), "UTF-8");
        } catch (UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException e1) {
            e1.printStackTrace();
        }

        return decrypted;
    }

    /**
     * This method updates BEER_FILE with the most recent data stored in
     * beerRatings. BEER_FILE is completely overwritten during the process. If
     * the JSON file cannot be found then a new file is created in the data
     * directory.
     * 
     * @throws JSONException
     *             if data could not be written in JSON format.
     * @throws IOException
     *             if data could not be written.
     */

    public void writeBeerData(Map<String, Integer> beerRatings) throws JSONException, IOException {
        if (Util.checkFileExists(fullBeerFilePath)) {
            
            if (beerRatings.keySet().size() > 0) { 
                JSONObject jsonToWrite = new JSONObject();
                JSONArray jsonBeerList = new JSONArray();

                for (String beerName : beerRatings.keySet()) {
                    JSONObject jsonBeer = new JSONObject();
                    jsonBeer.put(beerName, beerRatings.get(beerName));
                    jsonBeerList.put(jsonBeer);
                }

                jsonToWrite.put("beers", jsonBeerList);
                FileWriter writer = new FileWriter(fullBeerFilePath, false);
                writer.write(jsonToWrite.toString());
                writer.flush();
                writer.close();

                beerDataReady = true;
            } else {
                File file = new File(fullBeerFilePath);
                file.delete();
                beerDataReady = false;
            }

        } else {
            createFileInDataDirectory(BEER_FILE);

            if (beerRatings.keySet().size() > 0) {
                JSONObject jsonToWrite = new JSONObject();
                JSONArray jsonBeerList = new JSONArray();

                for (String beerName : beerRatings.keySet()) {
                    JSONObject jsonBeer = new JSONObject();
                    jsonBeer.put(beerName, beerRatings.get(beerName));
                    jsonBeerList.put(jsonBeer);
                }

                jsonToWrite.put("beers", jsonBeerList);
                FileWriter writer = new FileWriter(fullBeerFilePath);
                writer.write(jsonToWrite.toString());
                writer.flush();
                writer.close();

                beerDataReady = true;
            } else {
                beerDataReady = false;
            }

        }
    }

    /**
     * Reads and parses BEER_FILE and loads the data into beerRatings and beers.
     * Does not modify BEER_FILE in any way.
     */
    public Map<String, Integer> readBeerData() {
        Map<String, Integer> beerRatings = new HashMap<String, Integer>();

        try {
            String jsonString = Util.readFileToString(fullBeerFilePath);
            if (jsonString == null) {
                beerDataReady = false;
                return beerRatings;
            }

            beerDataReady = false;

            JSONObject obj = (JSONObject) new JSONTokener(jsonString).nextValue();
            JSONArray jsonBeers = obj.getJSONArray("beers");

            for (int i = 0; i < jsonBeers.length(); i++) {
                JSONObject nextBeer = jsonBeers.getJSONObject(i);
                String beerName = nextBeer.keys().next().toString();

                beerRatings.put(beerName, nextBeer.getInt(beerName));
                // beers.add(beerName);
                beerDataReady = true;
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            beerDataReady = false;
        }

        return beerRatings;
    }

    /**
     * Creates a new file given by fileName in the data directory of this
     * program. The data directory location is determined by the location of the
     * .jar file that corresponds to this program. If a file already exists at
     * that location with the same name then no new file is created.
     */
    private void createFileInDataDirectory(String fileName) {
        String newDir = "";
        String path = Util.getJarPath();

        String fileSeparator = System.getProperty("file.separator");
        newDir = path + "data" + fileSeparator;
        // JOptionPane.showMessageDialog(null, newDir);

        File file = new File(newDir + fileName);
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates and writes the binary SALT value to SALT_FILE.
     */
    private void writeSALT() {
        createFileInDataDirectory(SALT_FILE);
        SecureRandom secureRandom = new SecureRandom();
        SALT = secureRandom.generateSeed(8);

        try {
            FileOutputStream fos = new FileOutputStream(fullSaltPath);
            fos.write(SALT);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the binary SALT value from SALT_FILE.
     */
    private void readSALT() {
        File saltFile = new File(fullSaltPath);
        SALT = new byte[(int) saltFile.length()];

        try {
            FileInputStream fis = new FileInputStream(fullSaltPath);
            fis.read(SALT);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

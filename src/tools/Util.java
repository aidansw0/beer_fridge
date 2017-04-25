package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import backend.SaveData;

/**
 * Contains several static utility methods.
 * 
 * @author Aidan
 *
 */
public final class Util {

    /**
     * Makes a deep copy of the given map.
     * 
     * @param map
     *            Map<String, Integer> to be copied.
     * @return Map<String, Integer> deep copy of map.
     */
    public static Map<String, Integer> deepCopy(Map<String, Integer> map) {
        Map<String, Integer> copy = new HashMap<String, Integer>();

        for (String str : map.keySet()) {
            copy.put(new String(str), map.get(str).intValue());
        }

        return copy;
    }

    /**
     * Converts the given Map<String, Integer> keySet() to a LinkedList<String>
     * containing the exact same elements. The elements in the returned list are
     * not ordered in any specific way.
     * 
     * @param map
     *            Map<String, Integer> whose keySet() is to be converted to a
     *            List.
     * @return LinkedList<String> containing the exact same elements as
     *         map.keySet() in no specific order.
     */
    public static List<String> toList(Map<String, Integer> map) {
        List<String> list = new LinkedList<String>();

        for (String str : map.keySet()) {
            list.add(str);
        }

        return list;
    }

    /**
     * Reads the contents of the file at filePath into a single string.
     * 
     * @param filePath
     *            the full file path of the file to be read. This file must
     *            exist.
     * @return String with the contents of the file.
     * @throws IOException
     */
    public static String readFileToString(String filePath) throws IOException {
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

    /**
     * Checks weather a file at fullPath exists
     * 
     * @return true if a file at fullPath exists and false if otherwise.
     */
    public static boolean checkFileExists(String fullPath) {
        File testFile = null;
        testFile = new File(fullPath);
        return testFile.exists();
    }

    /**
     * Locates the directory in which the .jar executable file for this
     * application exits.
     * 
     * @return String giving the full path of the .jar executable including a
     *         system dependent file separator at the end of the path.
     */
    public static String getJarPath() {
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

}

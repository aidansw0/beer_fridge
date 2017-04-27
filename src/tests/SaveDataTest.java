package tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.junit.Ignore;
import org.junit.Test;

import backend.SaveData;

public class SaveDataTest {

    @Ignore
    public void addUserTest() {
        SaveData save = new SaveData();

        save.addUser("user0");
        save.addUser("user1");
        save.addUser("user2");
        save.addUser("user3");
        save.addUser("user4");

        try {
            save.writeUsersToFile();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Ignore
    public void checkUserExistsTest() {
        SaveData save = new SaveData();

        assertTrue(save.checkUserExists("user0"));
        assertTrue(save.checkUserExists("user1"));
        assertTrue(save.checkUserExists("user2"));
        assertTrue(save.checkUserExists("user3"));
        assertTrue(save.checkUserExists("user4"));
    }

    @Ignore
    public void checkFlagsFalseTest() {
        SaveData save = new SaveData();

        assertEquals(false, save.checkAdmin("user0"));
        assertEquals(false, save.checkAdmin("user1"));
        assertEquals(false, save.checkAdmin("user2"));
        assertEquals(false, save.checkAdmin("user3"));
        assertEquals(false, save.checkAdmin("user4"));

        assertEquals(false, save.checkVoted("user0"));
        assertEquals(false, save.checkVoted("user1"));
        assertEquals(false, save.checkVoted("user2"));
        assertEquals(false, save.checkVoted("user3"));
        assertEquals(false, save.checkVoted("user4"));

        save.setAdmin("user0", true);
        save.setAdmin("user1", true);
        save.setAdmin("user2", true);
        save.setAdmin("user3", true);
        save.setAdmin("user4", true);

        save.setVoted("user0", true);
        save.setVoted("user1", true);
        save.setVoted("user2", true);
        save.setVoted("user3", true);
        save.setVoted("user4", true);

        try {
            save.writeUsersToFile();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Ignore
    public void checkFlagsTrueTest() {
        SaveData save = new SaveData();

        assertEquals(true, save.checkAdmin("user0"));
        assertEquals(true, save.checkAdmin("user1"));
        assertEquals(true, save.checkAdmin("user2"));
        assertEquals(true, save.checkAdmin("user3"));
        assertEquals(true, save.checkAdmin("user4"));

        assertEquals(true, save.checkVoted("user0"));
        assertEquals(true, save.checkVoted("user1"));
        assertEquals(true, save.checkVoted("user2"));
        assertEquals(true, save.checkVoted("user3"));
        assertEquals(true, save.checkVoted("user4"));
    }

    @Ignore
    public void addExistingUsers() {
        SaveData save = new SaveData();

        save.addUser("user0");

        assertEquals(true, save.checkAdmin("user0"));
        assertEquals(true, save.checkVoted("user0"));
    }

    @Test
    public void writeCurrentBeerTest() {
        SaveData save = new SaveData();

        try {
            save.writeCurrentKeg("beer0", 100.1);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        Object[] obj = save.readCurrentKeg();

        assertEquals("beer0", obj[0]);
        assertEquals(100.1, obj[1]);
    }

    @Test
    public void readCurrentBeerTest() {
        SaveData save = new SaveData();
        Object[] obj = save.readCurrentKeg();

        assertEquals("beer0", obj[0]);
        assertEquals(100.1, obj[1]);
    }

}

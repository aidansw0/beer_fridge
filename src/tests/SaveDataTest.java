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
    
    @Test
    public void addUserTest() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        List<String> list = new ArrayList<String>();
        SaveData save = new SaveData(map, list);
        
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
    
    @Test
    public void checkUserExistsTest() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        List<String> list = new ArrayList<String>();
        SaveData save = new SaveData(map, list);    
        
        assertTrue(save.checkUserExists("user0"));
        assertTrue(save.checkUserExists("user1"));
        assertTrue(save.checkUserExists("user2"));
        assertTrue(save.checkUserExists("user3"));
        assertTrue(save.checkUserExists("user4"));
    }
    
    @Ignore
    public void checkFlagsFalseTest() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        List<String> list = new ArrayList<String>();
        SaveData save = new SaveData(map, list);
        
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
        Map<String, Integer> map = new HashMap<String, Integer>();
        List<String> list = new ArrayList<String>();
        SaveData save = new SaveData(map, list);
        
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
        Map<String, Integer> map = new HashMap<String, Integer>();
        List<String> list = new ArrayList<String>();
        SaveData save = new SaveData(map, list);
        
        save.addUser("user0");
        
        assertEquals(true, save.checkAdmin("user0"));
        assertEquals(true, save.checkVoted("user0"));
    }

}

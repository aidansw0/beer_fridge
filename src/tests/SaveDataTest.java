package tests;

import static org.junit.Assert.*;

import java.io.IOException;
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
        Map<String, Integer> map = new HashMap<String, Integer>();
        List<String> list = new ArrayList<String>();
        SaveData save = new SaveData(map, list);
        
        save.addUserToBuffer("user0");
        save.addUserToBuffer("user1");
        save.addUserToBuffer("user2");
        save.addUserToBuffer("user3");
        save.addUserToBuffer("user4");
        
        try {
            save.writeBufferToFile();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
    
    @Ignore
    public void checkUserExistsTest() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        List<String> list = new ArrayList<String>();
        SaveData save = new SaveData(map, list);
        
        assertTrue(save.checkUserInBuffer("user0"));
        assertTrue(save.checkUserInBuffer("user1"));
        assertTrue(save.checkUserInBuffer("user2"));
        assertTrue(save.checkUserInBuffer("user3"));
        assertTrue(save.checkUserInBuffer("user4"));
    }
    
    @Test
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
            save.writeBufferToFile();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

}

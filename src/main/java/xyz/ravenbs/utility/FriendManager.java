package xyz.ravenbs.utility;

import java.util.ArrayList;
import java.util.List;

public class FriendManager {
    public static List<String> friends = new ArrayList<>();
    
    public static void addFriend(String name) {
        if (!friends.contains(name.toLowerCase())) {
            friends.add(name.toLowerCase());
            save();
            Utils.sendMessage("§aAdded friend: " + name);
        }
    }
    
    public static void removeFriend(String name) {
        if (friends.remove(name.toLowerCase())) {
            save();
            Utils.sendMessage("§cRemoved friend: " + name);
        } else {
            Utils.sendMessage("§cFriend not found: " + name);
        }
    }
    
    public static boolean isFriended(String name) {
        return friends.contains(name.toLowerCase());
    }
    
    public static void clear() {
        friends.clear();
        save();
        Utils.sendMessage("§cCleared all friends.");
    }

    private static java.io.File getFile() {
        return new java.io.File(xyz.ravenbs.config.ConfigManager.getConfigDirectory(), "friends.json");
    }

    public static void save() {
        java.io.File file = getFile();
        com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
        try (java.io.Writer writer = new java.io.FileWriter(file)) {
            gson.toJson(friends, writer);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        java.io.File file = getFile();
        if (!file.exists()) return;
        
        com.google.gson.Gson gson = new com.google.gson.Gson();
        try (java.io.Reader reader = new java.io.FileReader(file)) {
            java.util.List<String> loaded = gson.fromJson(reader, new com.google.gson.reflect.TypeToken<java.util.List<String>>(){}.getType());
            if (loaded != null) {
                friends = loaded;
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    static {
        load();
    }
}

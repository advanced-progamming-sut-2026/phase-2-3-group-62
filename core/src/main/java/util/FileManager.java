package util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.user.Settings;
import model.user.User;
import network.Message;
import network.NetworkManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private static final String SETTINGS_FILE = "settings.json";
    private static final String CACHED_USER_FILE = "cached_session.json";
    private static final Gson gson = new Gson();

    public static synchronized void saveCachedUser(User user) {
        if (user == null) {
            File file = new File(CACHED_USER_FILE);
            if (file.exists()) file.delete();
            return;
        }
        try (FileWriter writer = new FileWriter(CACHED_USER_FILE)) {
            gson.toJson(user, writer);
        } catch (IOException ignored) {}
    }

    public static synchronized User loadCachedUser() {
        File file = new File(CACHED_USER_FILE);
        if (!file.exists()) return null;
        try (FileReader reader = new FileReader(file)) {
            return gson.fromJson(reader, User.class);
        } catch (IOException e) {
            return null;
        }
    }

    public static synchronized List<User> loadUsers() {
        Message req = new Message(Message.Type.LOAD_ALL_USERS);
        Message resp = NetworkManager.getInstance().sendRequest(req);
        if (resp.getType() == Message.Type.SUCCESS && resp.get("users_json") != null) {
            return gson.fromJson(resp.get("users_json"), new TypeToken<List<User>>(){}.getType());
        }
        return new ArrayList<>();
    }

    public static synchronized void saveUsers(List<User> users) {
        for (User u : users) {
            updateUser(u);
        }
    }

    public static synchronized boolean isUsernameExists(String username) {
        User u = getUser(username);
        return u != null;
    }

    public static synchronized boolean checkPassword(String username, String hashedPassword) {
        User u = getUser(username);
        return u != null && u.getPassword().equals(hashedPassword);
    }

    public static synchronized User getUser(String username) {
        if (username == null) return null;
        Message req = new Message(Message.Type.GET_USER).put("username", username.trim());
        Message resp = NetworkManager.getInstance().sendRequest(req);
        if (resp.getType() == Message.Type.SUCCESS && resp.get("user_json") != null) {
            return gson.fromJson(resp.get("user_json"), User.class);
        }
        return null;
    }

    public static synchronized void updateUser(User updatedUser) {
        if (updatedUser == null) return;
        saveCachedUser(updatedUser);
        Message req = new Message(Message.Type.UPDATE_USER).put("user_json", gson.toJson(updatedUser));
        NetworkManager.getInstance().sendRequest(req);
    }

    public static synchronized Settings loadSettings() {
        File file = new File(SETTINGS_FILE);
        if (!file.exists()) {
            return new Settings();
        }
        try (FileReader reader = new FileReader(file)) {
            Settings settings = gson.fromJson(reader, Settings.class);
            return (settings != null) ? settings : new Settings();
        } catch (IOException e) {
            return new Settings();
        }
    }

    public static synchronized void saveSettings(Settings settings) {
        try (FileWriter writer = new FileWriter(SETTINGS_FILE)) {
            gson.toJson(settings, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

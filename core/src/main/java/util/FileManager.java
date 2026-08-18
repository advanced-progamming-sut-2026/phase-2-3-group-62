package util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.user.Settings;
import model.user.User;

import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private static final String FILE_PATH = "database/users.json";
    private static final String SETTINGS_PATH = "database/settings.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void saveUsers(List<User> users) {
        try {
            FileHandle file = Gdx.files.local(FILE_PATH);
            file.writeString(gson.toJson(users), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateUser(User updatedUser) {
        List<User> users = loadUsers();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equalsIgnoreCase(updatedUser.getUsername())) {
                users.set(i, updatedUser);
                break;
            }
        }
        saveUsers(users);
    }

    public static Settings loadSettings() {
        try {
            FileHandle file = Gdx.files.local(SETTINGS_PATH);
            if (!file.exists()) {
                file = Gdx.files.internal(SETTINGS_PATH);
            }
            if (!file.exists()) {
                Settings defaultSettings = new Settings();
                saveSettings(defaultSettings);
                return defaultSettings;
            }
            Settings settings = gson.fromJson(file.readString(), Settings.class);
            return (settings != null) ? settings : new Settings();
        } catch (Exception e) {
            return new Settings();
        }
    }

    public static void saveSettings(Settings settings) {
        try {
            FileHandle file = Gdx.files.local(SETTINGS_PATH);
            file.writeString(gson.toJson(settings), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isUsernameExists(String username) {
        List<User> users = loadUsers();
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkPassword(String username, String hashedConfirmPassword) {
        List<User> users = loadUsers();
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user.getPassword().equals(hashedConfirmPassword);
            }
        }
        return false;
    }

    public static List<User> loadUsers() {
        try {
            FileHandle file = Gdx.files.local(FILE_PATH);
            if (!file.exists()) {
                file = Gdx.files.internal(FILE_PATH);
            }
            if (!file.exists()) {
                return new ArrayList<>();
            }
            List<User> users = gson.fromJson(file.readString(), new TypeToken<List<User>>(){}.getType());
            return (users != null) ? users : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static User getUser(String username) {
        List<User> users = loadUsers();
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    public void changeDifficulty(int newDifficulty) {
        Settings settings = FileManager.loadSettings();
        settings.setDifficulty(newDifficulty);
        FileManager.saveSettings(settings);
    }
}

package network;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.user.User;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServerFileManager {
    private static final String USERS_FILE = "database/users.json";
    private static final Gson gson = new Gson();

    public static synchronized List<User> loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            file = new File("phase-1-group-62-main/database/users.json");
        }
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (FileReader reader = new FileReader(file)) {
            List<User> users = gson.fromJson(reader, new TypeToken<List<User>>(){}.getType());
            return (users != null) ? users : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public static synchronized void saveUsers(List<User> users) {
        File file = new File(USERS_FILE);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(users, writer);
        } catch (IOException ignored) {}
    }

    public static synchronized boolean isUsernameExists(String username) {
        if (username == null) return false;
        List<User> users = loadUsers();
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username.trim())) {
                return true;
            }
        }
        return false;
    }

    public static synchronized User getUser(String username) {
        if (username == null) return null;
        List<User> users = loadUsers();
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username.trim())) {
                return u;
            }
        }
        return null;
    }

    public static synchronized void addUser(User user) {
        if (user == null) return;
        List<User> users = loadUsers();
        users.removeIf(u -> u.getUsername().equalsIgnoreCase(user.getUsername()));
        users.add(user);
        saveUsers(users);
    }

    public static synchronized void updateUser(User user) {
        addUser(user);
    }
}

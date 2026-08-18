package model;

import model.user.User;

import java.util.ArrayList;
import java.util.List;

public class Model {
    private final List<User> users;
    private User currentUser;
    private Game currentGame;

    public Model() {
        this.users = new ArrayList<>();
    }

    public List<User> getUsers() {
        return users;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public Game getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(Game currentGame) {
        this.currentGame = currentGame;
    }
}


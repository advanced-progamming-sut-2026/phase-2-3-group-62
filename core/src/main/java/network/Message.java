package network;

import java.util.HashMap;
import java.util.Map;

public class Message {
    public enum Type {
        REGISTER,
        LOGIN,
        FORGET_PASSWORD,
        RESET_PASSWORD,
        GET_USER,
        UPDATE_USER,
        LOAD_ALL_USERS,
        SUCCESS,
        ERROR,
        MATCHMAKING_JOIN,
        MATCHMAKING_LEAVE,
        MATCHMAKING_FOUND,
        CHALLENGE_REQUEST,
        CHALLENGE_RECEIVED,
        CHALLENGE_RESPONSE,
        CHALLENGE_ACCEPTED,
        CHALLENGE_REJECTED,
        GAME_PLAYER_READY,
        GAME_ACTION_PLANT,
        GAME_ACTION_SPAWN_ZOMBIE,
        GAME_STATE_UPDATE,
        GAME_REACTION,
        GAME_OVER
    }

    private Type type;
    private Map<String, String> data = new HashMap<>();

    public Message() {}

    public Message(Type type) {
        this.type = type;
    }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public Message put(String key, String value) {
        data.put(key, value);
        return this;
    }

    public String get(String key) {
        return data.get(key);
    }

    public Map<String, String> getData() {
        return data;
    }
}

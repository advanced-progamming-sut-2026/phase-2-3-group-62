package network;

import com.google.gson.Gson;
import model.enums.Gender;
import model.user.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson = new Gson();
    private String authenticatedUsername = null;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String line;
            while ((line = in.readLine()) != null) {
                Message request = gson.fromJson(line, Message.class);
                if (request == null || request.getType() == null) continue;
                Message response = handleMessage(request);
                if (response != null) {
                    sendDirectMessage(response);
                }
            }
        } catch (IOException ignored) {
        } finally {
            cleanup();
        }
    }

    private Message handleMessage(Message request) {
        switch (request.getType()) {
            case REGISTER: {
                String u = request.get("username");
                String p = request.get("password");
                String n = request.get("nickname");
                String e = request.get("email");
                String g = request.get("gender");
                String q = request.get("question");
                String a = request.get("answer");

                if (ServerFileManager.isUsernameExists(u)) {
                    return new Message(Message.Type.ERROR).put("message", "Username already exists.");
                }

                Gender genderEnum = "female".equalsIgnoreCase(g) ? Gender.FEMALE : Gender.MALE;
                User newUser = new User(u, p, n, e, genderEnum, q, a);
                ServerFileManager.addUser(newUser);
                return new Message(Message.Type.SUCCESS).put("message", "Registered successfully.");
            }

            case LOGIN: {
                String u = request.get("username");
                String p = request.get("password");
                User user = ServerFileManager.getUser(u);

                if (user == null || !user.getPassword().equals(p)) {
                    return new Message(Message.Type.ERROR).put("message", "Invalid username or password.");
                }

                this.authenticatedUsername = user.getUsername();
                GameServer.registerOnlineClient(this.authenticatedUsername, this);
                return new Message(Message.Type.SUCCESS).put("user_json", gson.toJson(user));
            }

            case GET_USER: {
                String u = request.get("username");
                User user = ServerFileManager.getUser(u);
                if (user != null) {
                    if (this.authenticatedUsername == null) {
                        this.authenticatedUsername = user.getUsername();
                        GameServer.registerOnlineClient(this.authenticatedUsername, this);
                    }
                    return new Message(Message.Type.SUCCESS).put("user_json", gson.toJson(user));
                }
                return new Message(Message.Type.ERROR).put("message", "User not found.");
            }

            case UPDATE_USER: {
                String json = request.get("user_json");
                User user = gson.fromJson(json, User.class);
                if (user != null) {
                    ServerFileManager.updateUser(user);
                    this.authenticatedUsername = user.getUsername();
                    GameServer.registerOnlineClient(this.authenticatedUsername, this);
                    return new Message(Message.Type.SUCCESS).put("message", "Updated.");
                }
                return new Message(Message.Type.ERROR).put("message", "Failed to update user.");
            }

            case LOAD_ALL_USERS: {
                return new Message(Message.Type.SUCCESS).put("users_json", gson.toJson(ServerFileManager.loadUsers()));
            }

            case MATCHMAKING_JOIN: {
                if (authenticatedUsername == null) {
                    return new Message(Message.Type.ERROR).put("message", "You must be logged in.");
                }
                GameServer.joinRandomQueue(this);
                return null;
            }

            case MATCHMAKING_LEAVE: {
                GameServer.leaveRandomQueue(this);
                return new Message(Message.Type.SUCCESS).put("message", "Left queue.");
            }

            case CHALLENGE_REQUEST: {
                String targetUsername = request.get("target_username");
                if (authenticatedUsername == null) {
                    return new Message(Message.Type.ERROR).put("message", "You must be logged in.");
                }
                if (authenticatedUsername.equalsIgnoreCase(targetUsername)) {
                    return new Message(Message.Type.ERROR).put("message", "Cannot challenge yourself.");
                }

                User targetDb = ServerFileManager.getUser(targetUsername);
                if (targetDb == null) {
                    return new Message(Message.Type.ERROR).put("message", "User not found.");
                }

                ClientHandler targetHandler = GameServer.getOnlineClient(targetUsername);
                if (targetHandler == null) {
                    return new Message(Message.Type.ERROR).put("message", "User is currently offline.");
                }

                Message invite = new Message(Message.Type.CHALLENGE_RECEIVED)
                    .put("from_username", this.authenticatedUsername);
                targetHandler.sendDirectMessage(invite);

                return new Message(Message.Type.SUCCESS).put("message", "Invitation sent. Waiting for response...");
            }

            case CHALLENGE_RESPONSE: {
                String fromUser = request.get("from_username");
                String response = request.get("response");
                ClientHandler senderHandler = GameServer.getOnlineClient(fromUser);

                if ("ACCEPT".equalsIgnoreCase(response)) {
                    if (senderHandler != null) {
                        Message acceptMsg = new Message(Message.Type.CHALLENGE_ACCEPTED)
                            .put("opponent_username", this.authenticatedUsername)
                            .put("role", "PLANTS");
                        senderHandler.sendDirectMessage(acceptMsg);

                        Message myStart = new Message(Message.Type.CHALLENGE_ACCEPTED)
                            .put("opponent_username", fromUser)
                            .put("role", "ZOMBIES");
                        sendDirectMessage(myStart);
                    } else {
                        return new Message(Message.Type.ERROR).put("message", "Player disconnected.");
                    }
                } else {
                    if (senderHandler != null) {
                        Message rejectMsg = new Message(Message.Type.CHALLENGE_REJECTED)
                            .put("from_username", this.authenticatedUsername)
                            .put("message", "Invitation was declined.");
                        senderHandler.sendDirectMessage(rejectMsg);
                    }
                }
                return null;
            }

            case GAME_PLAYER_READY:
            case GAME_ACTION_PLANT:
            case GAME_ACTION_SPAWN_ZOMBIE:
            case GAME_STATE_UPDATE:
            case GAME_REACTION:
            case GAME_OVER: {
                String target = request.get("opponent_username");
                if (target != null) {
                    ClientHandler handler = GameServer.getOnlineClient(target);
                    if (handler != null) {
                        handler.sendDirectMessage(request);
                    }
                }
                return null;
            }

            default:
                return new Message(Message.Type.ERROR).put("message", "Unknown action.");
        }
    }

    public synchronized void sendDirectMessage(Message msg) {
        if (out != null) {
            out.println(gson.toJson(msg));
        }
    }

    public String getAuthenticatedUsername() {
        return authenticatedUsername;
    }

    private void cleanup() {
        GameServer.leaveRandomQueue(this);
        if (authenticatedUsername != null) {
            GameServer.unregisterOnlineClient(authenticatedUsername);
        }
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }
}

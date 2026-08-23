package network;

import com.google.gson.Gson;
import model.user.User;
import model.user.UserSession;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NetworkManager {
    private static NetworkManager instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson = new Gson();

    private String host = "127.0.0.1";
    private int port = 8080;
    private volatile boolean isConnected = false;

    private final Queue<Message> incomingPushMessages = new ConcurrentLinkedQueue<>();
    private Thread listenerThread;

    private NetworkManager() {}

    public static synchronized NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    public synchronized boolean connect(String host, int port) {
        this.host = host;
        this.port = port;
        return reconnect();
    }

    public synchronized boolean reconnect() {
        disconnect();
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 1200);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            isConnected = true;

            User current = UserSession.getCurrentUser();
            if (current != null) {
                Message syncReq = new Message(Message.Type.UPDATE_USER)
                    .put("user_json", gson.toJson(current));
                out.println(gson.toJson(syncReq));
            }

            startListener();
            return true;
        } catch (Exception e) {
            disconnect();
            return false;
        }
    }

    private void startListener() {
        listenerThread = new Thread(() -> {
            try {
                String line;
                while (isConnected && in != null && (line = in.readLine()) != null) {
                    Message msg = gson.fromJson(line, Message.class);
                    if (msg != null) {
                        incomingPushMessages.add(msg);
                    }
                }
            } catch (Exception ignored) {
            } finally {
                disconnect();
            }
        }, "NetworkListenerThread");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public synchronized void sendAsync(Message msg) {
        if (!isConnected) reconnect();
        if (isConnected && out != null) {
            out.println(gson.toJson(msg));
        }
    }

    public Message pollPushMessage() {
        return incomingPushMessages.poll();
    }

    public synchronized Message sendRequest(Message request) {
        if (!isConnected) reconnect();
        if (!isConnected || out == null) {
            return new Message(Message.Type.ERROR).put("message", "Server is offline.");
        }
        try {
            out.println(gson.toJson(request));
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 3000) {
                Message msg = pollPushMessage();
                if (msg != null) {
                    return msg;
                }
                Thread.sleep(20);
            }
            return new Message(Message.Type.ERROR).put("message", "Response timeout.");
        } catch (Exception e) {
            disconnect();
            return new Message(Message.Type.ERROR).put("message", "Request failed: " + e.getMessage());
        }
    }

    public synchronized boolean isConnected() {
        if (!isConnected) {
            return reconnect();
        }
        return true;
    }

    public synchronized void disconnect() {
        isConnected = false;
        try {
            if (socket != null && !socket.isClosed()) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (Exception ignored) {}
        in = null;
        out = null;
        socket = null;
        if (listenerThread != null && listenerThread.isAlive()) {
            listenerThread.interrupt();
        }
    }
}

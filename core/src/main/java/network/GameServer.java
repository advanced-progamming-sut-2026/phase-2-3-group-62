package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer {
    private final int port;
    private volatile boolean isRunning = false;
    private ServerSocket serverSocket;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    private static final Map<String, ClientHandler> onlineUsers = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<ClientHandler> matchmakingQueue = new ConcurrentLinkedQueue<>();

    public GameServer(int port) {
        this.port = port;
    }

    public static void registerOnlineClient(String username, ClientHandler handler) {
        if (username != null && handler != null) {
            onlineUsers.put(username.toLowerCase(), handler);
        }
    }

    public static void unregisterOnlineClient(String username) {
        if (username != null) {
            onlineUsers.remove(username.toLowerCase());
        }
    }

    public static ClientHandler getOnlineClient(String username) {
        if (username == null) return null;
        return onlineUsers.get(username.toLowerCase());
    }

    public static synchronized void joinRandomQueue(ClientHandler client) {
        if (matchmakingQueue.contains(client)) return;

        if (!matchmakingQueue.isEmpty()) {
            ClientHandler opponent = matchmakingQueue.poll();
            if (opponent != null && opponent != client) {
                Message match1 = new Message(Message.Type.MATCHMAKING_FOUND)
                    .put("opponent_username", opponent.getAuthenticatedUsername())
                    .put("role", "PLANTS");
                Message match2 = new Message(Message.Type.MATCHMAKING_FOUND)
                    .put("opponent_username", client.getAuthenticatedUsername())
                    .put("role", "ZOMBIES");

                client.sendDirectMessage(match1);
                opponent.sendDirectMessage(match2);
                return;
            }
        }
        matchmakingQueue.add(client);
    }

    public static synchronized void leaveRandomQueue(ClientHandler client) {
        matchmakingQueue.remove(client);
    }

    public void start() {
        isRunning = true;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            System.out.println("[SERVER] Server started on port " + port);

            Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

            Thread consoleListener = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                    String line;
                    while (isRunning && (line = reader.readLine()) != null) {
                        if ("exit".equalsIgnoreCase(line.trim()) || "stop".equalsIgnoreCase(line.trim())) {
                            stop();
                            break;
                        }
                    }
                } catch (IOException ignored) {}
            });
            consoleListener.setDaemon(true);
            consoleListener.start();

            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.execute(new ClientHandler(clientSocket));
                } catch (IOException e) {
                    if (!isRunning) break;
                }
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Listen error: " + e.getMessage());
        } finally {
            stop();
        }
    }

    public synchronized void stop() {
        if (!isRunning && (serverSocket == null || serverSocket.isClosed())) return;
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}
        threadPool.shutdownNow();
    }

    public static void main(String[] args) {
        new GameServer(8080).start();
    }
}

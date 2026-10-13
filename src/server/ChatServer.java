package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatServer {

    private final int port = 5000;
    private ServerSocket serverSocket;
    private final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final Map<ClientHandler, String> userMap = new ConcurrentHashMap<>();
    //server start
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                handler.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addUser(ClientHandler handler, String username) {
        userMap.put(handler, username);
        broadcastUserList();
    }

    public void removeUser(ClientHandler handler) {
        clients.remove(handler);
        userMap.remove(handler);
        broadcastUserList();
    }
//user list broadcast to all connected users
    private void broadcastUserList() {
        String userList = String.join(",", userMap.values());
        String message = "USERLIST:" + userList;
        for (ClientHandler client : userMap.keySet()) {
            client.sendMessage(message);
        }
    }

    // Forward a private message to a single user
    public void sendPrivate(String targetUsername, String taggedMessage) {
        for (Map.Entry<ClientHandler, String> entry : userMap.entrySet()) {
            if (entry.getValue().equals(targetUsername)) {
                entry.getKey().sendMessage(taggedMessage); // send the Lamport-tagged message
                break;
            }
        }
    }
}

package server;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {

    private final Socket socket;
    private final ChatServer server;
    private PrintWriter out;
    private String username;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out = new PrintWriter(socket.getOutputStream(), true);

            // First message from client is the username
            username = in.readLine();
            server.addUser(this, username);  

            String line;
           
            while ((line = in.readLine()) != null) {
                if (line.startsWith("PRIVATE:")) {
                    String[] parts = line.split(":", 5); // PRIVATE:sender:target:msg:clock
                    if (parts.length == 5) {
                        String target = parts[2];
                        server.sendPrivate(target, line); // forward full line including Lamport timestamp
                    }
                } else {
                    // other message types if any
                }
            }

        } catch (IOException ignored) {
        } finally {
            server.removeUser(this);  
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public String getUsername() {
        return username;
    }
}

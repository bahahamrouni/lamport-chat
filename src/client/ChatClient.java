package client;

import client.ui.MultiChatWindow;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private MultiChatWindow ui;

    private final String host = "127.0.0.1";
    private final int port = 5000;
    private String username;

    //// Lamport clock part ////////////
    private int lamportClock = 0;

    public synchronized int getLamportClock() {
        return lamportClock;
    }

    public synchronized void incrementLamportClock() {
        lamportClock++;
    }

    public synchronized void updateLamportClock(int receivedClock) {
        lamportClock = Math.max(lamportClock, receivedClock) + 1;
    }
    ///////////////////////////////////

    public void start() {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // username ui
            username = JOptionPane.showInputDialog(null, "Enter your username:", "Username", JOptionPane.PLAIN_MESSAGE);
            if (username == null || username.isEmpty()) {
                username = "Client" + (int)(Math.random() * 1000);
            }
            out.println(username);

            // multi tab
            ui = new MultiChatWindow(this);
            ui.updateLamportLabel(); // initial Lamport label

            // thread
            Thread listener = new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        if (line.startsWith("PRIVATE:")) {
                            // Format: PRIVATE:sender:target:message:clock
                            String[] parts = line.split(":", 5);
                            if (parts.length == 5) {
                                String sender = parts[1];
                                String message = parts[3];
                                int receivedClock = Integer.parseInt(parts[4]);

                                // Update local Lamport clock
                                updateLamportClock(receivedClock);

                                // Display message in the correct tab
                                ui.appendMessage(sender, sender + " [Lamport:" + receivedClock + "]: " + message);

                                // Update Lamport label in UI
                                ui.updateLamportLabel();
                            }
                        } else if (line.startsWith("USERLIST:")) {
                            // Update user list in UI
                            String[] users = line.substring(9).split(",");
                            ui.updateUserList(users);
                        }
                    }
                } catch (IOException ignored) {}
            });
            listener.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Send a private message with Lamport timestamp automatically included */
    public void sendPrivate(String targetUser, String message) {
        if (out != null && targetUser != null && !message.isEmpty()) {
            incrementLamportClock(); // increment before sending
            int clock = getLamportClock();
            String taggedMsg = "PRIVATE:" + username + ":" + targetUser + ":" + message + ":" + clock;
            sendRaw(taggedMsg);

            // Update Lamport label in UI
            if (ui != null) {
                ui.updateLamportLabel();
            }
        }
    }

    /** Send any raw string to server */
    public void sendRaw(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    /** Gracefully disconnect from server */
    public void disconnect() {
        try {
            if (out != null) out.close();
            if (in  != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }

    /** Return username */
    public String getUsername() {
        return username;
    }
}

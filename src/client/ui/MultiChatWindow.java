package client.ui;

import client.ChatClient;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MultiChatWindow extends JFrame {

    private final JList<String> userList = new JList<>();
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final ChatClient client;

    private final Map<String, JTextArea> chatAreas = new HashMap<>();
    private final Map<String, JScrollPane> scrollPanes = new HashMap<>();

    // Lamport clock display
    private final JLabel lamportLabel = new JLabel("Lamport Clock: 0");

    public MultiChatWindow(ChatClient client) {
        this.client = client;

        setTitle("Private Multi-Chat");
        setSize(600, 400);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Top panel: Lamport clock
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(lamportLabel);
        add(topPanel, BorderLayout.NORTH);

        // Left panel: user list
        userList.setModel(listModel);
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setPreferredSize(new Dimension(150, 0));
        add(userScroll, BorderLayout.WEST);

        // Center panel: tabbed chat area
        add(tabbedPane, BorderLayout.CENTER);

        // Bottom panel: input + send
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JTextField inputField = new JTextField();
        JButton sendButton = new JButton("Send");
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // User selection from list: create tab if needed
        userList.addListSelectionListener(e -> {
            String selectedUser = userList.getSelectedValue();
            if (selectedUser != null) {
                showTabForUser(selectedUser);
            }
        });

        // Send action
        sendButton.addActionListener(e -> sendMessage(inputField));
        inputField.addActionListener(e -> sendMessage(inputField));

        setVisible(true);
    }

    /** Creates a new chat area */
    private JTextArea createChatArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        return area;
    }

    /** Show tab for a given user, creating if needed */
    private void showTabForUser(String user) {
        if (!chatAreas.containsKey(user)) {
            JTextArea newChat = createChatArea();
            chatAreas.put(user, newChat);
            JScrollPane scrollPane = new JScrollPane(newChat);
            scrollPanes.put(user, scrollPane);
            tabbedPane.addTab(user, scrollPane);
            tabbedPane.setSelectedComponent(scrollPane);
        } else {
            tabbedPane.setSelectedComponent(scrollPanes.get(user));
        }
    }

    /** Update the user list in the UI */
    public void updateUserList(String[] users) {
        SwingUtilities.invokeLater(() -> {
            listModel.clear();
            for (String u : users) {
                if (!u.equals(client.getUsername())) { // optional: exclude self
                    listModel.addElement(u);
                }
            }
        });
    }

    /** Append a message to the correct user tab (auto-creates tab if needed) */
    public void appendMessage(String user, String message) {
        SwingUtilities.invokeLater(() -> {
            if (!chatAreas.containsKey(user)) {
                showTabForUser(user);
            }
            JTextArea chatArea = chatAreas.get(user);
            if (chatArea != null) {
                chatArea.append(message + "\n");
                chatArea.setCaretPosition(chatArea.getDocument().getLength()); // auto-scroll
            }
        });
    }

    /** Update Lamport clock label */
    public void updateLamportLabel() {
        SwingUtilities.invokeLater(() -> lamportLabel.setText("Lamport Clock: " + client.getLamportClock()));
    }

    /** Send a message from the input field to the selected user */
    private void sendMessage(JTextField inputField) {
        int selectedTab = tabbedPane.getSelectedIndex();
        if (selectedTab >= 0) {
            String targetUser = tabbedPane.getTitleAt(selectedTab);
            String msg = inputField.getText();
            if (!msg.isEmpty()) {
                client.incrementLamportClock(); // increment before sending
                int clock = client.getLamportClock();
                updateLamportLabel(); // update UI
                String taggedMsg = "PRIVATE:" + client.getUsername() + ":" + targetUser + ":" + msg + ":" + clock;
                client.sendRaw(taggedMsg); // send to server
                appendMessage(targetUser, "Me -> " + targetUser + " [Lamport:" + clock + "]: " + msg);
                inputField.setText("");
            }
        }
    }
}

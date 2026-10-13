package client;

import java.io.BufferedReader;
import java.io.IOException;

public class MessageListener extends Thread {

    private final BufferedReader in;

    public MessageListener(BufferedReader in) {
        this.in = in;
    }

    @Override
    public void run() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println(msg);
            }
        } catch (IOException ignored) {}
    }
}

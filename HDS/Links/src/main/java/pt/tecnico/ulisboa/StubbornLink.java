package pt.tecnico.ulisboa;

import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;

public class StubbornLink {
    private final FLL fll;
    private final int maxAttempts;
    private final int maxDelay;

    public StubbornLink(String hostname, int port, int maxAttempts, int maxDelay)
            throws SocketException, UnknownHostException {
        this.maxAttempts = maxAttempts;
        this.maxDelay = maxDelay;
        this.fll = new FLL(hostname, port);
    }

    public void send(String message, String hostName, int port) throws IOException, InterruptedException {
        for (int attempts = 0; attempts < this.maxAttempts; attempts++) {
            try {
                System.out.println("Attempt: "+ attempts);
                String messageID = Utility.getMacFromJson(message);
                String messageReceived = this.fll.send(message.getBytes(), hostName, port);
                String messageIDReceived = Utility.getMacFromJson(messageReceived);
                JSONObject jsonObject = new JSONObject(messageReceived);
                String command = jsonObject.getString("command");
                if (messageID.equals(messageIDReceived)) return;
                TimeUnit.SECONDS.sleep(this.maxDelay);
            }
            catch (SocketTimeoutException e) {
                // timeout exception.
                System.out.println("Timeout reached!!! " + e);
            }
        }

    }

    public String receive() throws IOException {
        return this.fll.receive();
    }
}

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
    private ConcurrentHashMap<String, JSONObject> acksReceived;

    public StubbornLink(String hostname, int port, int maxAttempts, int maxDelay, ConcurrentHashMap<String, JSONObject> acksReceived)
            throws SocketException, UnknownHostException {
        this.maxAttempts = maxAttempts;
        this.maxDelay = maxDelay;
        this.fll = new FLL(hostname, port);
        this.acksReceived = acksReceived;
    }

    public void send(String message, String hostName, int port) throws IOException, InterruptedException {
        String messageID = Utility.getMacFromJson(message);
        int attempts = 0;
        while (!this.acksReceived.containsKey(messageID) && attempts < this.maxAttempts) {
            System.out.println("Attempt: "+ attempts);
            this.fll.send(message.getBytes(), hostName, port);
            attempts++;
            TimeUnit.SECONDS.sleep(this.maxDelay);
        }
       this.acksReceived.remove(messageID);
    }

    public String receive() throws IOException {
        return this.fll.receive();
    }
}

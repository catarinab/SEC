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
    private double delay;
    private final int maxAttempts;
    private ConcurrentHashMap<String, JSONObject> acksReceived;

    public StubbornLink(String hostname, int port, int maxAttempts, double delay, ConcurrentHashMap<String, JSONObject> acksReceived)
            throws SocketException, UnknownHostException {
        this.maxAttempts = maxAttempts;
        this.delay = delay;
        this.fll = new FLL(hostname, port);
        this.acksReceived = acksReceived;
    }

    public void send(String message, String hostName, int port) throws IOException, InterruptedException {
        String messageID = Utility.getMacFromJson(message);
        int attempts = this.maxAttempts;
        while (!this.acksReceived.containsKey(messageID)) {
            if (attempts == 0) {
                attempts = this.maxAttempts;
                this.delay *= this.delay; //increase delay exponentally
            }
            this.fll.send(message.getBytes(), hostName, port);
            attempts--;
            long delay = (long) this.delay;
            TimeUnit.SECONDS.sleep(delay);
        }
       this.acksReceived.remove(messageID);
    }

    public String receive() throws IOException {
        return this.fll.receive();
    }

    public FLL getFll() {
        return fll;
    }
}

package pt.tecnico.ulisboa;

import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;

public class StubbornLink {
    private final FLL fll;
    private final int maxAttempts;
    private final int maxDelay;
    private ConcurrentHashMap<String, String> ACKs = new ConcurrentHashMap<String, String>();

    public StubbornLink(String hostname, int port, int maxAttempts, int maxDelay)
            throws SocketException, UnknownHostException {
        this.maxAttempts = maxAttempts;
        this.maxDelay = maxDelay;
        this.fll = new FLL(hostname, port);
    }

    public void send(String message, String hostName, int port) throws IOException, InterruptedException {
        for (int attempts = 0; attempts < this.maxAttempts; attempts++) {
            String messageID = Utility.getMacFromJson(message);

            if(ACKs.containsKey(messageID)) {
                ACKs.remove(messageID);
                return;
            }
            String messageReceived = this.fll.send(message.getBytes(), hostName, port);
            String messageIDReceived = Utility.getMacFromJson(messageReceived);
            JSONObject jsonObject = new JSONObject(messageReceived);
            String command = jsonObject.getString("command");
            if(messageID.equals(messageIDReceived) && command.equals("ack")) return;
            else if(messageIDReceived != null) ACKs.put(messageIDReceived, "ack");
            TimeUnit.SECONDS.sleep(this.maxDelay);
        }
    }

    public String receive() throws IOException {
        return this.fll.receive();
    }
}

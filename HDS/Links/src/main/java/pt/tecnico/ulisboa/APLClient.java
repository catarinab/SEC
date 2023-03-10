package pt.tecnico.ulisboa;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.json.JSONObject;
import pt.tecnico.ulisboa.Utility.Type;

public class APLClient {
    private final StubbornLink stubbornLink;

    private int messageCounter = 0;

    public APLClient(String hostname, int port, Type type) throws SocketException, UnknownHostException {
        this.stubbornLink = new StubbornLink(hostname, port, 10, 2, type);
    }

    public boolean send(String message) throws IOException, InterruptedException {
        this.messageCounter++;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("messageID", String.valueOf(this.messageCounter));
        jsonObject.put("append", message);
        return this.stubbornLink.send(jsonObject.toString());
    }
}

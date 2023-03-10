package pt.tecnico.ulisboa;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import org.json.JSONObject;
import pt.tecnico.ulisboa.Utility.Type;

//Por enquanto fica apenas PL.
public class APLServer {
    private final StubbornLink stubbornLink;
    private ArrayList<String> delivered = new ArrayList<>();

    public APLServer(String hostname, int port, Type type) throws SocketException, UnknownHostException {
        this.stubbornLink = new StubbornLink(hostname, port, 10, 2, type);
    }

    public void send(String message, String hostName, int port) throws IOException, InterruptedException {
        this.stubbornLink.send(message, hostName, port);
    }

    public String receive() throws IOException {
        String message = this.stubbornLink.receive();
        try {
            JSONObject jsonObject = new JSONObject(message);
            String key = jsonObject.keys().next();
            if (key.equals("messageID")) {
                String messageID = jsonObject.getString("messageID");
                if (!delivered.contains(messageID)) {
                    delivered.add(messageID);
                    return jsonObject.getString("append");
                }
            }
            else if (key.equals("serverID")) return null;
        }
        catch(Exception e){
            return null;
        }
        return null;
    }
}

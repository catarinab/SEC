package pt.tecnico.ulisboa;

import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import pt.tecnico.ulisboa.Utility;

public class Client {
    private final Entry<String,Integer> processID;
    private final APL apl;
    private Broadcast broadcast;
    private int messageCounter = 0;


    public Client(List<Entry<String,Integer>> processes) throws SocketException, UnknownHostException {
        processID = new AbstractMap.SimpleEntry<>("localhost", 4321);
        this.apl = new APL("localhost", 4321);
        this.broadcast = new Broadcast(processes, this.apl);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        List<Entry<String,Integer>> processes = Utility.readProcesses("/home/rita/SEC/HDS/services.txt");

        System.out.println(Client.class.getName());
        Client client = new Client(processes);
        client.send("hey pu$$y queen");
    }

    public void send(String message) throws IOException, InterruptedException {
        this.messageCounter++; //substituido por MAC
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("messageID", this.processID.getKey() + String.valueOf(this.processID.getValue()) + String.valueOf(this.messageCounter));
        jsonObject.put("command", "append");
        jsonObject.put("message", message);
        this.broadcast.doBroadcast(jsonObject.toString());
    }

}
package pt.tecnico.ulisboa;

import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;

public class Client extends Thread{
    private final Entry<String,Integer> processID;
    private final Broadcast broadcast;
    private final APL apl;
    private final Mac mac = Mac.getInstance("HmacSHA256");
    private final Key key;
    private static final String RSA = "DES";
    private ConcurrentHashMap<String, JSONObject> acksReceived = new ConcurrentHashMap<>();



    public Client(List<Entry<String,Integer>> processes) throws SocketException, UnknownHostException,
            NoSuchAlgorithmException, InvalidKeyException {
        processID = new AbstractMap.SimpleEntry<>("localhost", 4321);
        this.apl = new APL("localhost", 4321, acksReceived);
        this.broadcast = new Broadcast(processes, apl);
        KeyGenerator keyGen = KeyGenerator.getInstance("DES");
        SecureRandom secRandom = new SecureRandom();
        keyGen.init(secRandom);
        this.key = keyGen.generateKey();
        mac.init(key);
    }

    public Client(Client client) throws NoSuchAlgorithmException {
        processID = client.processID;
        this.apl = client.apl;
        this.broadcast = client.broadcast;
        this.key = null;
        this.acksReceived = client.acksReceived;

    }

    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException,
            InvalidKeyException {
        List<Entry<String,Integer>> processes = Utility.readProcesses("/home/cat/uni/mestrado/SEC/HDS/services.txt").getValue();

        System.out.println(Client.class.getName());
        Client client = new Client(processes);
        Client thread = new Client(client);
        thread.start();
        client.send("ola");
    }

    public void send(String message) throws IOException, InterruptedException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command", "append");
        jsonObject.put("inputValue", message);
        this.broadcast.doBroadcast(message + "append", jsonObject.toString());
    }
    public void run() {
        for(int received = 0; received < 4; received++){
            try{
                String message = this.apl.receive();
                System.out.println(message);
                JSONObject jsonObject = new JSONObject(message);
                String messageID = jsonObject.getString("mac");
                if (jsonObject.getString("command").equals("ack")) acksReceived.put(messageID, jsonObject);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
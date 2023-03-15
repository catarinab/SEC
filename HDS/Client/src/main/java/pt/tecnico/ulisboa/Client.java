package pt.tecnico.ulisboa;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.*;

public class Client extends Thread{
    private final Entry<String,Integer> processID;
    private final int numProcesses;
    private final Broadcast broadcast;
    private final APL apl;
    private final Mac mac = Mac.getInstance("HmacSHA256");
    private static final String RSA = "DES";
    private ConcurrentHashMap<String, JSONObject> acksReceived = new ConcurrentHashMap<>();


    public Client(List<Entry<String,Integer>> processes) throws IOException,
            NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        this.processID = new AbstractMap.SimpleEntry<>("localhost", 4321);
        this.numProcesses = processes.size();
        this.apl = new APL("localhost", 4321, acksReceived);
        this.broadcast = new Broadcast(processes, apl);
    }

    public Client(Client client) throws NoSuchAlgorithmException {
        this.processID = client.processID;
        this.numProcesses = client.numProcesses;
        this.apl = client.apl;
        this.broadcast = client.broadcast;
        this.acksReceived = client.acksReceived;

    }

    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {

        String path = System.getProperty("path");
        List<Entry<String,Integer>> processes = Utility.readProcesses(path).getValue();

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

    public void receive() throws IOException {
        String message = this.apl.receive();
        System.out.println(message);
        JSONObject jsonObject = new JSONObject(message);
        String messageID = jsonObject.getString("mac");
        if (jsonObject.getString("command").equals("ack")) acksReceived.put(messageID, jsonObject);
    }

    public void run() {
        for(int received = 0; received < numProcesses; received++){
            try {
                receive();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
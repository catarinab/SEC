package pt.tecnico.ulisboa;

import org.json.JSONObject;

import java.io.IOException;
import java.security.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Scanner;

import javax.crypto.*;

public class Client extends Thread{
    private final Entry<String,Integer> processID;
    private final int numProcesses;

    private final Broadcast broadcast;
    private final APL apl;
    private ConcurrentHashMap<String, JSONObject> acksReceived = new ConcurrentHashMap<>();


    public Client(List<Entry<String,Integer>> processes) throws IOException,
            NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException {
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
            InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {

        System.out.println(Client.class.getName());

        String path = System.getProperty("path");
        List<Entry<String,Integer>> processes = Utility.readProcesses(path).getValue();

        Client client = new Client(processes);
        Client thread = new Client(client);
        thread.start();
        // Loop to read messages from the user and send them to the server
        while (true) {
            Scanner input = new Scanner(System.in);
            System.out.print("Enter a string to append to the blockchain: ");
            String message = input.nextLine();
            client.send(message);

        }
    }

    public void send(String message) throws IOException, InterruptedException, NoSuchPaddingException,
            IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command", "append");
        jsonObject.put("inputValue", message);
        this.broadcast.doBroadcast(message + "append", jsonObject.toString());
    }

    public void receive() throws IOException {
        String message = this.apl.receive();
        JSONObject jsonObject = new JSONObject(message);
        String messageID = jsonObject.getString("mac");
        if (jsonObject.getString("command").equals("ack")) acksReceived.put(messageID, jsonObject);
        else if (jsonObject.getString("command").equals("decide")) {
            System.out.println("\nThe string \"" + jsonObject.getString("inputValue") + "\" has been appended to the blockchain.");
        }
    }

    public void run() {
        while (true) {
            try {
                receive();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
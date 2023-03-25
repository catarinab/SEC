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
    private final List<Entry<String,Integer>> processes;
    private final int byzantineProcesses;
    private ConcurrentHashMap<Integer,ConcurrentHashMap<Entry<String,Integer>,String>> decideMessages = new ConcurrentHashMap<>();
    private List<Integer> consensusDecided = new java.util.ArrayList<>();

    private final Broadcast broadcast;
    private final APL apl;
    private ConcurrentHashMap<String, JSONObject> acksReceived = new ConcurrentHashMap<>();


    public Client(String hostname, int port, List<Entry<String,Integer>> processes, int byzantineProcesses) throws IOException,
            NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException {
        this.processID = new AbstractMap.SimpleEntry<>(hostname, port);
        this.numProcesses = processes.size();
        this.processes = processes;
        this.byzantineProcesses = byzantineProcesses;
        this.apl = new APL(hostname, port, acksReceived);
        this.broadcast = new Broadcast(processes, apl);
    }

    public Client(Client client) throws NoSuchAlgorithmException {
        this.processID = client.processID;
        this.numProcesses = client.numProcesses;
        this.processes = client.processes;
        this.byzantineProcesses = client.byzantineProcesses;
        this.decideMessages =client.decideMessages;
        this.apl = client.apl;
        this.broadcast = client.broadcast;
        this.acksReceived = client.acksReceived;

    }

    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException,
            InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {

        System.out.println(Client.class.getName());

        String hostname = System.getProperty("hostname");
        int port = Integer.parseInt(System.getProperty("port"));
        String path = System.getProperty("path");

        Entry<Integer, List<Entry<String,Integer>>> fileSetup = Utility.readProcesses(path);
        int byzantineProcesses = fileSetup.getKey();
        List<Entry<String,Integer>> processes = fileSetup.getValue();

        Client client = new Client(hostname, port, processes, byzantineProcesses);
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
            int consensusID = jsonObject.getInt("consensusID");
            String receivedHostname = jsonObject.getString("hostname");
            int receivedPort = jsonObject.getInt("port");
            if (!this.consensusDecided.contains(consensusID)) {
                String inputValue = jsonObject.getString("inputValue");
                Entry<String, Integer> receivedProcess = null;
                for (Entry<String, Integer> process : this.processes) {
                    if (process.getKey().equals(receivedHostname) && process.getValue() == receivedPort) {
                        receivedProcess = process;
                        break;
                    }
                }
                if (!this.decideMessages.contains(consensusID)) {
                    this.decideMessages.put(consensusID, new ConcurrentHashMap<>());
                    this.decideMessages.get(consensusID).put(receivedProcess, inputValue);
                }
                else if (!this.decideMessages.get(consensusID).containsKey(receivedProcess)) {
                    this.decideMessages.get(consensusID).put(receivedProcess, inputValue);
                    int quorumSize = 2 * this.byzantineProcesses + 1;
                    if (this.decideMessages.size() >= quorumSize) {
                        int validCounter = 0;

                        for (Entry<String, Integer> key: this.decideMessages.get(consensusID).keySet()) {
                            if (this.decideMessages.get(key).equals(inputValue)) validCounter++;
                        }
                        if (validCounter >= quorumSize) {
                            this.decideMessages.remove(consensusID);
                            this.consensusDecided.add(consensusID);
                            System.out.println("\nThe string \"" + inputValue + "\" has been appended to the blockchain.");
                        }
                    }
                }
            }
        }
    }

    public APL getApl() {
        return apl;
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
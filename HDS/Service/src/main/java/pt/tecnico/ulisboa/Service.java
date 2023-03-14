package pt.tecnico.ulisboa;

import org.json.JSONObject;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
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
import java.util.concurrent.TimeUnit;
import javax.crypto.Mac;

public class Service extends Thread {
    private final Entry<String,Integer> processID;
    private final APL apl;
    private final Broadcast broadcast;
    private boolean leader = false;
    private ArrayList<String> delivered = new ArrayList<>();
    private ConcurrentHashMap<String, JSONObject> acksReceived = new ConcurrentHashMap<>();
    private int messageCounter = 0;
    private int byzantineProcesses = 0;
    private ConcurrentHashMap<Integer, IstanbulBFT> consensusInstances = new ConcurrentHashMap<>();
    private int consensusCounter = 0;

    private JSONObject message = null;

    public Service(String hostname, int port, int byzantineProcesses, List<Entry<String,Integer>> processes, boolean leader)
            throws SocketException, UnknownHostException, NoSuchAlgorithmException, InvalidKeyException {
        processID = new AbstractMap.SimpleEntry<>(hostname, port);
        this.apl = new APL(hostname, port, acksReceived);
        this.leader = leader;
        this.broadcast = new Broadcast(processes, this.apl);
        this.byzantineProcesses = byzantineProcesses;
    }

    public Service(Service father, JSONObject message) throws NoSuchAlgorithmException, InvalidKeyException {
        this.processID = father.processID;
        this.apl = father.apl;
        this.broadcast = father.broadcast;
        this.leader = father.leader;
        this.delivered = father.delivered;
        this.acksReceived = father.acksReceived;
        this.messageCounter = father.messageCounter;
        this.byzantineProcesses = father.byzantineProcesses;
        this.consensusInstances = father.consensusInstances;
        this.consensusCounter = father.consensusCounter;
        this.message = message;
    }


    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException,
            InvalidKeyException {
        String hostname = null;
        int port = 0;
        if(args.length != 2) serviceUsage();
        try {
            hostname = args[0];
            port = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException nfe) {
            serviceUsage();
        }

        Entry<Integer, List<Entry<String,Integer>>> fileSetup = Utility.readProcesses("/home/cat/uni/mestrado/SEC/HDS/services.txt");
        int byzantineProcesses = fileSetup.getKey();
        List<Entry<String,Integer>> processes = fileSetup.getValue();
        boolean leader = false;
        for (int i = 0; i < processes.size(); i++) {
            if (processes.get(i).getKey().equals(hostname) && processes.get(i).getValue() == port) {
                if (i == 0) leader = true;
                processes.remove(i);
                break;
            }
        }

        Service service = new Service(hostname, port, byzantineProcesses, processes, leader);
        System.out.println(Service.class.getName());
        while(true) service.receive();
    }

    public static void serviceUsage() {
        System.out.println("Usage: Service port");
        System.out.println("port is an int with a maximum of 5 chars");
        System.exit(1);
    }

    public void receive() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        String message = this.apl.receive();

        JSONObject jsonObject = new JSONObject(message);
        String messageID = jsonObject.getString("mac");
        if (jsonObject.getString("command").equals("ack")) acksReceived.put(messageID, jsonObject);
        else if (!delivered.contains(messageID)) {
            delivered.add(messageID);
            Service thread = new Service(this, jsonObject);
            thread.start();
        }
    }

    public void run() {
        System.out.println("This code is running in a thread with message: " + this.message);
        String command = this.message.getString("command");
        if (command.equals("append")) {

            IstanbulBFT istanbulBFT = null;
            try {
                istanbulBFT = new IstanbulBFT(this.processID, this.leader, this.broadcast,
                        this.byzantineProcesses);
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
            this.messageCounter++;
            this.consensusCounter++;
            try {
                istanbulBFT.algorithm1(this.consensusCounter, this.message.getString("inputValue"), this.messageCounter);
                this.consensusInstances.put(consensusCounter, istanbulBFT);
                if (this.leader) istanbulBFT.algorithm2("pre-prepare", this.message.getString("inputValue"), this.messageCounter);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (command.equals("pre-prepare") || command.equals("prepare") || command.equals("commit")) {
            this.messageCounter++;
            try {
                for (int i = 0; i < 7; i++) {
                    try {
                        this.consensusInstances.get(this.message.getInt("consensusID")).algorithm2(command,
                                this.message.getString("inputValue"), this.messageCounter);
                        break;
                    }
                    catch (Exception e){
                        TimeUnit.SECONDS.sleep(1);
                    }
                }
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
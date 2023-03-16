package pt.tecnico.ulisboa;

import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Service extends Thread {
    private final Entry<String,Integer> processID;
    private final boolean byzantine;
    private final int byzantineProcesses;
    private final boolean leader;

    private final APL apl;
    private final Broadcast broadcast;
    private ArrayList<String> delivered = new ArrayList<>();
    private ConcurrentHashMap<String, JSONObject> acksReceived = new ConcurrentHashMap<>();

    private Blockchain blockchain = new Blockchain();

    private volatile static int consensusCounter = 0;
    private ConcurrentHashMap<Integer, IstanbulBFT> consensusInstances = new ConcurrentHashMap<>();
    private JSONObject message = null;

    public Service(String hostname, int port, boolean byzantine, int byzantineProcesses, List<Entry<String,Integer>> processes,
                    boolean leader) throws IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException,
                    IllegalBlockSizeException, BadPaddingException {
        this.processID = new AbstractMap.SimpleEntry<>(hostname, port);
        this.byzantine = byzantine;
        this.byzantineProcesses = byzantineProcesses;
        this.apl = new APL(hostname, port, acksReceived);
        this.leader = leader;
        this.broadcast = new Broadcast(processes, this.apl);
    }

    public Service(Service father, JSONObject message) {
        this.processID = father.processID;
        this.byzantine = father.byzantine;
        this.byzantineProcesses = father.byzantineProcesses;
        this.apl = father.apl;
        this.broadcast = father.broadcast;
        this.leader = father.leader;
        this.delivered = father.delivered;
        this.acksReceived = father.acksReceived;
        this.blockchain = father.blockchain;
        this.consensusInstances = father.consensusInstances;
        this.consensusCounter = father.consensusCounter;
        this.message = message;
    }


    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeyException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {

        String behavior = System.getProperty("behaviour");
        String server = System.getProperty("server");
        String path = System.getProperty("path");

        String[] serverInfo = server.split(" ");
        String hostname = serverInfo[0];
        int port = Integer.parseInt(serverInfo[1]);

        if(serverInfo.length != 2 || (!behavior.equals("B") && !behavior.equals("C"))) serviceUsage();
        try {
            hostname = serverInfo[0];
            port = Integer.parseInt(serverInfo[1]);
        }
        catch (NumberFormatException nfe) {
            serviceUsage();
        }

        Entry<Integer, List<Entry<String,Integer>>> fileSetup = Utility.readProcesses(path);
        int byzantineProcesses = fileSetup.getKey();
        List<Entry<String,Integer>> processes = fileSetup.getValue();
        boolean leader = processes.get(0).getKey().equals(hostname) && processes.get(0).getValue() == port;
        if (behavior.equals("B") && leader) {
            System.out.println("The leader cannot have byzantine behavior.");
            System.exit(1);
        }

        Service service = new Service(hostname, port, behavior.equals("B"), byzantineProcesses, processes, leader);
        System.out.println(Service.class.getName());
        while (true) service.receive();
    }

    public static void serviceUsage() {
        System.out.println("Usage: Service hostname port behavior.");
        System.out.println("hostname: domain name assigned to a host computer.");
        System.out.println("port: Integer that identifies connection endpoint, with a maximum of 5 characters.");
        System.out.println("behavior: C (Correct) or B (Byzantine).");
        System.exit(1);
    }

    public void receive() throws IOException {
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
        String command = this.message.getString("command");
        String inputValue = this.message.getString("inputValue");
        if (byzantine) {
            StringBuilder reverse = new StringBuilder();
            for (int i = 0; i < inputValue.length(); i++) reverse.insert(0, inputValue.charAt(i));
            inputValue = reverse.toString();
        }
        System.out.println("This code is running in a thread with message: " + "(" + command + ") " + inputValue);
        if (command.equals("append")) {
            int consensusID;
            synchronized (this) {
                consensusID = this.consensusCounter++;
            }

            IstanbulBFT istanbulBFT;
            try {
                istanbulBFT = new IstanbulBFT(this.processID, this.leader, this.broadcast,
                        this.byzantineProcesses, this.blockchain);
                this.consensusInstances.put(consensusID, istanbulBFT);
                TimeUnit.SECONDS.sleep(1);
                istanbulBFT.algorithm1(consensusID, inputValue);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (command.equals("pre-prepare") || command.equals("prepare") || command.equals("commit")) {
            try {
                this.consensusInstances.get(this.message.getInt("consensusID")).algorithm2(command,
                        this.message.getString("inputValue"));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
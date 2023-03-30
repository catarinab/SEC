package pt.tecnico.ulisboa;

import org.json.JSONObject;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Service extends Thread {
    private final Entry<String,Integer> processID;
    private final List<Entry<String,Integer>> processes;
    private final boolean byzantine;
    private final int byzantineProcesses;
    private final boolean leader;
    private final Entry<String,Integer> leaderID;
    private final APL apl;
    private final Broadcast broadcast;
    private ArrayList<String> delivered = new ArrayList<>();
    private ConcurrentHashMap<String, JSONObject> acksReceived = new ConcurrentHashMap<>();
    private Blockchain blockchain = new Blockchain(10);
    //current state of accounts
    private final ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<>();

    static class ConsensusCounter {
        public volatile int counter = 0;
    }
    static class CurrentConsensus {
        public volatile int id = 0;
    }
    private ConsensusCounter consensusCounter = new ConsensusCounter();
    private CurrentConsensus currentConsensus = new CurrentConsensus();
    private ConcurrentHashMap<Integer, IstanbulBFT> consensusInstances = new ConcurrentHashMap<>();
    private JSONObject message = null;

    public Service(String hostname, int port, boolean byzantine, int byzantineProcesses, List<Entry<String,Integer>> processes,
                   boolean leader, Entry<String,Integer> leaderID) throws IOException, NoSuchAlgorithmException {
        this.processID = new AbstractMap.SimpleEntry<>(hostname, port);
        this.processes = processes;
        this.byzantine = byzantine;
        this.byzantineProcesses = byzantineProcesses;
        this.apl = new APL(hostname, port, acksReceived);
        this.leader = leader;
        this.leaderID = leaderID;
        this.broadcast = new Broadcast(processes, this.apl);
    }

    public Service(Service father, JSONObject message) {
        this.processID = father.processID;
        this.processes = father.processes;
        this.byzantine = father.byzantine;
        this.byzantineProcesses = father.byzantineProcesses;
        this.apl = father.apl;
        this.broadcast = father.broadcast;
        this.leader = father.leader;
        this.leaderID = father.leaderID;
        this.delivered = father.delivered;
        this.acksReceived = father.acksReceived;
        this.blockchain = father.blockchain;
        this.consensusInstances = father.consensusInstances;
        this.consensusCounter = father.consensusCounter;
        this.currentConsensus = father.currentConsensus;
        this.message = message;
    }


    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

        String behavior = System.getProperty("behaviour");
        String server = System.getProperty("server");
        String path = System.getProperty("path");

        String[] serverInfo = server.split(" ");
        String hostname = serverInfo[0];
        int port = Integer.parseInt(serverInfo[1]);

        if (serverInfo.length != 2 || (!behavior.equals("B") && !behavior.equals("C"))) serviceUsage();
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

        Service service = new Service(hostname, port, behavior.equals("B"), byzantineProcesses, processes, leader, processes.get(0));
        System.out.println(Service.class.getName());
        service.receive();
    }

    public static void serviceUsage() {
        System.out.println("Usage: Service hostname port behavior.");
        System.out.println("hostname: domain name assigned to a host computer.");
        System.out.println("port: Integer that identifies connection endpoint, with a maximum of 5 characters.");
        System.out.println("behavior: C (Correct) or B (Byzantine).");
        System.exit(1);
    }
    public boolean create_account(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if(this.accounts.containsKey(publicKey)) return false;
        byte[] decodedKey = Base64.getDecoder().decode(publicKey);
        PublicKey receivedKey = KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(decodedKey));
        Account newAcc = new Account(receivedKey);
        this.accounts.put(publicKey, new Account(receivedKey));
        OperationDTO op = new OperationDTO(publicKey, newAcc.check_balance(), 0, publicKey);
        this.blockchain.addOperation(op);
        return true;
    }
    public int check_balance(PublicKey key) {
        return this.accounts.get(key.toString()).check_balance();
    }

    //Como verificar q é mesmo a pessoa -> usar public key da source
    public boolean transfer(String source, String destination, int amount) {
        Account sourceAcc = this.accounts.get(source);
        Account destinationAcc = this.accounts.get(destination);
        if(amount > 0) {
            if(sourceAcc.check_balance() - amount < 0) return false;
            sourceAcc.removeBalance(amount);
            destinationAcc.addBalance(amount);
        }
        else if(amount < 0) {
            if(destinationAcc.check_balance() - amount < 0) return false;
            destinationAcc.removeBalance(amount);
            sourceAcc.addBalance(amount);
        }
        OperationDTO op = new OperationDTO(source, this.accounts.get(source).check_balance(), 0, source);
        this.blockchain.addOperation(op);
        return true;
    }
    public boolean isInBlockchain(String data) {
        return this.blockchain.getBlockchainData().contains(data);
    }

    public String getBlockchainIndex(int index) {
        return this.blockchain.getBlockchainIndex(index);
    }

    public List<String> getBlockchainData() {
        return this.blockchain.getBlockchainData();
    }

    public void receive() throws IOException {
        while (true) {
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
    }

    public void run() {
        String command = this.message.getString("command");
        String inputValue = this.message.getString("inputValue");
        String receivedHostname = this.message.getString("hostname");
        int receivedPort = this.message.getInt("port");
        if (byzantine) {
            StringBuilder reverse = new StringBuilder();
            for (int i = 0; i < inputValue.length(); i++) reverse.insert(0, inputValue.charAt(i));
            inputValue = reverse.toString();
        }
        System.out.println("This code is running in a thread with message: " + "(" + command + ") " + inputValue);
        if(command.equals("create_account")) {

        }
        if (command.equals("append")) {
            Entry<String,Integer> clientID = new AbstractMap.SimpleEntry<>(receivedHostname, receivedPort);
            int consensusID;
            synchronized (this.consensusCounter) {
                consensusID = this.consensusCounter.counter++;
            }

            synchronized (this.currentConsensus) {
                while (consensusID > this.currentConsensus.id) {
                    try {
                        this.currentConsensus.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            IstanbulBFT istanbulBFT;
            try {
                istanbulBFT = new IstanbulBFT(this.processID, clientID, this.leader, this.leaderID, this.apl,
                        this.broadcast, this.byzantineProcesses, this.blockchain, this.currentConsensus);
                synchronized (istanbulBFT) {
                    this.consensusInstances.put(consensusID, istanbulBFT);
                    TimeUnit.SECONDS.sleep(1);
                    istanbulBFT.algorithm1(consensusID, inputValue);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (command.equals("pre-prepare") || command.equals("prepare") || command.equals("commit")) {
            Entry<String,Integer> receivedProcess = null;
            for (Entry<String,Integer> process: this.processes) {
                if (process.getKey().equals(receivedHostname) && process.getValue() == receivedPort) {
                    receivedProcess = process;
                    break;
                }
            }
            IstanbulBFT istanbulBFT;
            try {
                synchronized (istanbulBFT = this.consensusInstances.get(this.message.getInt("consensusID"))) {
                    istanbulBFT.algorithm2(command, inputValue, receivedProcess);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public APL getApl() {
        return this.apl;
    }
}
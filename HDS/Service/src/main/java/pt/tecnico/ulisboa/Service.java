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
    private Blockchain blockchain = new Blockchain(3);
    //current state of accounts
    private ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<>();

    static class ConsensusCounter {
        public volatile int counter = 0;
    }
    static class CurrentConsensus {
        public volatile int id = 0;
    }
    private ConsensusCounter consensusCounter = new ConsensusCounter();
    private CurrentConsensus currentConsensus = new CurrentConsensus();
    private ConcurrentHashMap<Integer, IstanbulBFT> consensusInstances = new ConcurrentHashMap<>();
    private Block currBlock = new Block("", this.blockchain.getMaxTransactions());
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
        this.accounts = father.accounts;
        this.consensusInstances = father.consensusInstances;
        this.consensusCounter = father.consensusCounter;
        this.currentConsensus = father.currentConsensus;
        this.currBlock = father.currBlock;
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

    public void addCurrBlock(OperationDTO op) {
        try{
            if (!this.currBlock.addTransaction(op)) {
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
                if (byzantine) this.currBlock.byzantine();
                try {
                    istanbulBFT = new IstanbulBFT(this.processID, this.leader, this.leaderID, this.apl,
                            this.broadcast, this.byzantineProcesses, this.blockchain, this.currentConsensus);
                    synchronized (istanbulBFT) {
                        this.consensusInstances.put(consensusID, istanbulBFT);
                        TimeUnit.SECONDS.sleep(1);
                        istanbulBFT.algorithm1(consensusID, this.currBlock);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                String previousHash = this.currBlock.getHash();
                this.currBlock = new Block(previousHash, this.blockchain.getMaxTransactions());
                this.currBlock.addTransaction(op);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean create_account(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if(this.accounts.containsKey(publicKey)) return false;
        byte[] decodedKey = Base64.getDecoder().decode(publicKey);
        PublicKey receivedKey = KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(decodedKey));
        Account newAcc = new Account(receivedKey);
        this.accounts.put(publicKey, newAcc);
        OperationDTO op = new CreateAccDTO(publicKey, digSignature, newAcc.check_balance());
        this.addCurrBlock(op);
        return true;
    }

    public int check_balance(String publicKey) {
        if(this.accounts.containsKey(publicKey)){
            return this.accounts.get(publicKey).check_balance();
        }
        else{
            return -1;
        }
    }

    //Como verificar q é mesmo a pessoa -> usar public key da source
    public boolean transfer(String source, String destination, int amount, String digSignature) {
        Account sourceAcc = this.accounts.get(source);
        Account destinationAcc = this.accounts.get(destination);
        int prevBalanceSource = sourceAcc.check_balance();
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
        OperationDTO op = new TransferDTO(source, digSignature, this.accounts.get(source).check_balance(), prevBalanceSource,
                destination, amount);
        this.addCurrBlock(op);
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
        String receivedHostname = this.message.getString("hostname");
        int receivedPort = this.message.getInt("port");
        String digSignature = this.message.getString("mac");
        System.out.println("This code is running in a thread with command: " + command);
        switch (command) {
            case "create_account": {
                Entry<String, Integer> clientID = new AbstractMap.SimpleEntry<>(receivedHostname, receivedPort);
                String keyClient = this.message.getString("key");
                try {
                    if (!this.create_account(keyClient, digSignature)) return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case "transfer": {
                Entry<String, Integer> clientID = new AbstractMap.SimpleEntry<>(receivedHostname, receivedPort);
                String amount = this.message.getString("amount");
                String source = this.message.getString("key");
                String destination = this.message.getString("destination");
                if (!this.transfer(source, destination, Integer.parseInt(amount), digSignature)) return;
                break;
            }
            case "check_balance": {
                Entry<String, Integer> clientID = new AbstractMap.SimpleEntry<>(receivedHostname, receivedPort);
                String keyClient = this.message.getString("key");
                if (this.check_balance(keyClient) == -1) return;
                break;
            }
            //Consensus
            default:
                System.out.println("ConsensusID: " + this.message.getInt("consensusID"));
                String block = this.message.getString("inputValue");
                Block inputValue = new Block(new JSONObject(block));
                if (byzantine) inputValue.byzantine();

                Entry<String, Integer> receivedProcess = null;
                for (Entry<String, Integer> process : this.processes) {
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public APL getApl() {
        return this.apl;
    }
}
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
    private final int fee = 1;
    //current state of accounts
    private final ConcurrentHashMap<String, Account> accounts;

    static class WeakState {
        public boolean empty = true;
        public ConcurrentHashMap<String, Integer> state = new ConcurrentHashMap<>();
        public ConcurrentHashMap<Entry<String, Integer>, Entry<String, String>> signatures = new ConcurrentHashMap<>();
    }
    private WeakState weakState;
    private WeakState prevWeakState;
    private final int weakInterval = 3;

    static class ConsensusCounter {
        public volatile int counter = 0;
    }
    private final ConsensusCounter consensusCounter;
    static class CurrentConsensus {
        public volatile int id = 0;
    }
    private final CurrentConsensus currentConsensus;
    private ConcurrentHashMap<Integer, IstanbulBFT> consensusInstances = new ConcurrentHashMap<>();
    private Block currBlock = new Block("", this.blockchain.getMaxTransactions());
    private JSONObject message = null;

    public Service(String hostname, int port, boolean byzantine, int byzantineProcesses, List<Entry<String,Integer>> processes,
                   boolean leader, Entry<String,Integer> leaderID) throws IOException, NoSuchAlgorithmException {
        this.processID = new AbstractMap.SimpleEntry<>(hostname, port);
        this.processes = processes;
        this.byzantine = byzantine;
        this.byzantineProcesses = byzantineProcesses;
        this.accounts = new ConcurrentHashMap<>();
        this.weakState = new WeakState();
        this.prevWeakState = new WeakState();
        this.apl = new APL(hostname, port, acksReceived);
        this.leader = leader;
        this.leaderID = leaderID;
        this.broadcast = new Broadcast(processes, this.apl);
        this.consensusCounter = new ConsensusCounter();
        this.currentConsensus = new CurrentConsensus();
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
        this.weakState = father.weakState;
        this.prevWeakState = father.prevWeakState;
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
        try {
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

                this.currBlock.reset();

                synchronized (this.currentConsensus) {
                    try {
                        this.currentConsensus.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (consensusID % this.weakInterval == 0) {
                    String signature;
                    synchronized (this.weakState) {
                        if (!this.weakState.empty) {
                            this.prevWeakState.empty = false;
                            this.prevWeakState.state.clear();
                            this.prevWeakState.state.putAll(this.weakState.state);
                            this.prevWeakState.signatures.clear();
                            this.prevWeakState.signatures.putAll(this.weakState.signatures);
                        }
                        this.weakState.empty = false;
                        this.weakState.state.clear();
                        this.weakState.signatures.clear();
                        Block[] blocks = this.blockchain.getLastBlocks(3);
                        for (Block block : blocks) {
                            if (block == null) continue;
                            HashMap<String, Integer> accountsBalance = block.getAccountsBalance();
                            for (String publicKey : accountsBalance.keySet()) {
                                this.weakState.state.put(publicKey, accountsBalance.get(publicKey));
                            }
                        }
                    }
                    signature = apl.sign(this.weakStateToJsonObj(this.weakState.state).toString());
                    System.out.println("Weak state: " + weakState.state.size());
                    weakState.state.forEach((k,v)-> System.out.println("\t" + k + ", " + v));
                    
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("command", "weak_signature");
                    jsonObject.put("inputValue", signature);
                    TimeUnit.SECONDS.sleep(1);
                    this.broadcast.doBroadcast(signature + "weak_signature", jsonObject.toString());
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public JSONObject weakStateToJsonObj(ConcurrentHashMap<String, Integer> state) {
        JSONObject jsonObject = new JSONObject();
        for (String publicKey : state.keySet()) {
            jsonObject.put(publicKey, state.get(publicKey));
        }
        return jsonObject;
    }

    public JSONObject weakSignaturesToJsonObj(ConcurrentHashMap<Entry<String, Integer>, Entry<String, String>> signatures) {
        JSONObject jsonObject = new JSONObject();
        for (Entry<String,Integer> processID : signatures.keySet()) {
            Entry<String,String> signature = signatures.get(processID);
            JSONObject sigJsonObject = new JSONObject();
            sigJsonObject.put("key", signature.getKey());
            sigJsonObject.put("signature", signature.getValue());
            jsonObject.put(processID.getKey() + "," + Integer.toString(processID.getValue()), sigJsonObject);
        }
        return jsonObject;
    }

    public void sendErrorMessage(String inputValue, String digSignature, String hostname, int port, String errorCommand) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command", errorCommand);
        jsonObject.put("inputValue", inputValue);
        jsonObject.put("digSignature", digSignature);
        try {
            this.apl.send(inputValue + errorCommand, jsonObject.toString(), hostname, port);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String inputValue, String digSignature, String hostname, int port, String command) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command", command);
        jsonObject.put("inputValue", inputValue);
        jsonObject.put("digSignature", digSignature);
        try {
            this.apl.send(inputValue + command, jsonObject.toString(), hostname, port);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean create_account(String publicKey, String digSignature, String hostname, int port)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        System.out.println("Public Key: " + publicKey);
        byte[] decodedKey = Base64.getDecoder().decode(publicKey);
        PublicKey receivedKey = KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(decodedKey));
        Account newAcc = new Account(receivedKey);
        synchronized (this.accounts) {
            if (this.accounts.containsKey(publicKey)) return false;
            this.accounts.put(publicKey, newAcc);
        }
        OperationDTO op = new CreateAccDTO(publicKey, digSignature, newAcc.check_balance(), hostname, port);
        this.addCurrBlock(op);
        return true;
    }

    public int check_strong_balance(String publicKey) {
        if (!this.accounts.containsKey(publicKey)) return -1;
        return this.blockchain.check_balance(publicKey);
    }
    public int check_weak_balance(String publicKey) {
        if (!this.accounts.containsKey(publicKey)) return -1;
        return this.accounts.get(publicKey).check_balance();
    }

    public boolean transfer(String source, String destination, int amount, String digSignature, String hostname, int port) {
        if(source.equals(destination) || amount <= 0) return false;
        Account sourceAcc = this.accounts.get(source);
        Account destinationAcc = this.accounts.get(destination);
        synchronized (this.accounts) {
            int prevBalanceSource = sourceAcc.check_balance();
            int prevBalanceDest = destinationAcc.check_balance();
            if (sourceAcc.check_balance() - amount - this.fee < 0) return false;
            sourceAcc.removeBalance(amount + this.fee);
            destinationAcc.addBalance(amount);
            OperationDTO op = new TransferDTO(source, digSignature, prevBalanceSource, sourceAcc.check_balance(),
                    prevBalanceDest, destinationAcc.check_balance(), destination, amount, this.fee, hostname, port);
            this.addCurrBlock(op);
            return true;
        }
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
                String keyClient = this.message.getString("key");
                try {
                    if (!this.create_account(keyClient, digSignature, receivedHostname, receivedPort))
                        throw new Exception("account already exists");
                } catch (Exception except) {
                    String inputValue = "There was an error when creating the account. Your account might already exist.";
                    sendErrorMessage(inputValue, digSignature, receivedHostname, receivedPort, "error");
                }
                break;
            }
            case "transfer": {
                String amount = this.message.getString("amount");
                String source = this.message.getString("key");
                String destination = this.message.getString("destination");
                try {
                    if (!this.transfer(source, destination, Integer.parseInt(amount), digSignature, receivedHostname,
                            receivedPort))
                        throw new Exception("transfer could not be made");
                } catch (Exception e) {
                    String inputValue = "There was an error when executing the transfer. Please check if your account exist, " +
                            "if you have enough balance to complete your transfer (there is a fee per transaction in a block)" +
                            " or if you are not trying to transfer money to your own account by mistake.";
                    sendErrorMessage(inputValue, digSignature, receivedHostname, receivedPort, "error");

                }

                break;
            }
            case "check_balance": {
                String keyClient = this.message.getString("key");
                String message = "";
                String balanceCommand = "error";
                try {
                    if (this.message.getString("inputValue").equals("strong")) {
                        int balance = this.check_strong_balance(keyClient);
                        if (balance == -1) throw new Exception("Strongly consistent read - Your account may not exist or may not have been commited to the blockchain yet.");
                        message = Integer.toString(balance);
                        balanceCommand = "strong_balance";
                    }
                    else if (this.message.getString("inputValue").equals("weak")) {
                        synchronized (this.weakState) {
                            int quorumSize = 2 * this.byzantineProcesses + 1;
                            WeakState validWeakState;
                            if (this.weakState.empty || (this.weakState.signatures.size() < quorumSize && this.prevWeakState.empty)) {
                                balanceCommand = "error_weak";
                                throw new Exception("Weakly consistent read - The weak state hasn't been updated yet.");
                            }
                            else if (this.weakState.signatures.size() < quorumSize && !this.prevWeakState.empty)
                                validWeakState = this.prevWeakState;
                            else
                                validWeakState = this.weakState;
                            if (!validWeakState.state.containsKey(keyClient)) {
                                balanceCommand = "error_weak";
                                throw new Exception("Weakly consistent read - Your account may not exist or may not have been commited to the blockchain yet.");
                            }
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("weakState", this.weakStateToJsonObj(validWeakState.state).toString());
                            jsonObject.put("signatures", this.weakSignaturesToJsonObj(validWeakState.signatures));
                            jsonObject.put("sigNum", validWeakState.signatures.size());
                            message = jsonObject.toString();
                            balanceCommand = "weak_balance";
                        }
                    }
                    try {
                        sendMessage(message, digSignature, receivedHostname, receivedPort, balanceCommand);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } catch (Exception e) {
                    String inputValue = "There was an error when checking the balance: " + e.getMessage();
                    sendErrorMessage(inputValue, digSignature, receivedHostname, receivedPort, balanceCommand);
                }
                break;
            }
            case "weak_signature": {
                String signature = this.message.getString("inputValue");
                String key = message.getString("key");
                String hostPort = receivedHostname + "," + receivedPort;
                synchronized (this.weakState) {
                    try {
                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        if (Arrays.toString(digest.digest(this.weakStateToJsonObj(this.weakState.state).toString().getBytes())).equals(Arrays.toString(apl.unsign(signature, key, hostPort)))) {
                            this.weakState.signatures.put(new AbstractMap.SimpleEntry<>(receivedHostname, receivedPort), new AbstractMap.SimpleEntry<>(key, signature));
                        }
                        else System.out.println("Signature not valid.");
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("Weak signatures collected: " + weakState.signatures.size());
                break;
            }
            //Consensus
            default:
                System.out.println("ConsensusID: " + this.message.getInt("consensusID"));
                Block inputValue = new Block(new JSONObject(this.message.getString("inputValue")));
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
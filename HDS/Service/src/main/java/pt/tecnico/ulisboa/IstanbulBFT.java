package pt.tecnico.ulisboa;

import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class IstanbulBFT {
    private final Entry<String,Integer> processID;
    private final Entry<String,Integer> clientID;
    private boolean leader;
    private final Entry<String,Integer> leaderID;

    private APL apl;
    private Broadcast broadcast;
    private int byzantineProcesses;
    private int consensusID;

    private ConcurrentHashMap<Entry<String,Integer>,String> prepareMessages = new ConcurrentHashMap<>();
    private boolean commitPhase = false;
    private ConcurrentHashMap<Entry<String,Integer>,String> commitMessages = new ConcurrentHashMap<>();
    private boolean decisionPhase = false;

    //currentRound
    //processRound
    private String processValue;
    //private Timer timer = null;

    private Blockchain blockchain;

    public IstanbulBFT(Entry<String,Integer> processID, Entry<String,Integer> clientID, boolean leader, Entry<String,Integer> leaderID, APL apl, Broadcast broadcast, int byzantineProcesses,
                       Blockchain blockchain) throws NoSuchAlgorithmException, InvalidKeyException {
        this.processID = processID;
        this.clientID = clientID;
        this.leader = leader;
        this.leaderID = leaderID;
        this.apl = apl;
        this.broadcast = broadcast;
        this.byzantineProcesses = byzantineProcesses;
        this.blockchain = blockchain;
    }

    public synchronized void algorithm1(int consensusCounter, String message) throws IOException, InterruptedException,
            NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException,
            InvalidKeyException {
        this.consensusID = consensusCounter;
        //currentRound

        if (this.leader) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command", "pre-prepare");
            jsonObject.put("consensusID", this.consensusID);
            //currentRound
            jsonObject.put("inputValue", message);
            this.broadcast.doBroadcast(message + "pre-prepare", jsonObject.toString());
        }
        //timerRound
    }

    public synchronized void algorithm2(String command, String inputValue, Entry<String,Integer> receivedProcess) throws IOException, InterruptedException,
            NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException {
        if (command.equals("pre-prepare") && this.leaderID.equals(receivedProcess)) {
            //timerRound
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command", "prepare");
            jsonObject.put("consensusID", this.consensusID);
            //currentRound
            jsonObject.put("inputValue", inputValue);
            this.broadcast.doBroadcast(inputValue + "prepare", jsonObject.toString());
        }
        else if (command.equals("prepare") && !this.prepareMessages.containsKey(receivedProcess) && !this.commitPhase && !this.decisionPhase) {
            this.prepareMessages.put(receivedProcess, inputValue);
            int quorumSize = 2 * this.byzantineProcesses + 1;
            if (this.prepareMessages.size() >= quorumSize) {
                int validCounter = 0;
                for (Entry<String,Integer> key: this.prepareMessages.keySet()) {
                    if (this.prepareMessages.get(key).equals(inputValue)) validCounter++;
                }
                if (validCounter >= quorumSize) {
                    this.commitPhase = true;

                    //processRound
                    this.processValue = inputValue;

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("command", "commit");
                    jsonObject.put("consensusID", this.consensusID);
                    //currentRound
                    jsonObject.put("inputValue", inputValue);
                    this.broadcast.doBroadcast(inputValue + "commit", jsonObject.toString());
                }
            }
        }
        else if (command.equals("commit") && !this.commitMessages.containsKey(receivedProcess) && !decisionPhase) {
            //timerRound
            this.commitMessages.put(receivedProcess, inputValue);

            int quorumSize = 2 * this.byzantineProcesses + 1;
            if (this.commitMessages.size() >= quorumSize) {
                int validCounter = 0;

                for (Entry<String,Integer> key: this.commitMessages.keySet()) {
                    if (this.commitMessages.get(key).equals(inputValue)) validCounter++;
                }
                if (validCounter >= quorumSize) {
                    this.decisionPhase = true;
                    System.out.println("Istanbul BFT decided: " + inputValue);

                    //timerRound
                    this.blockchain.addValue(inputValue);
                    this.blockchain.printBlockchain();

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("command", "decide");
                    jsonObject.put("consensusID", this.consensusID);
                    jsonObject.put("inputValue", inputValue);
                    this.apl.send(inputValue + "decide", jsonObject.toString(), clientID.getKey(), clientID.getValue());
                }
            }
        }
    }
}
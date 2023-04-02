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
    private boolean leader;
    private final Entry<String,Integer> leaderID;

    private APL apl;
    private Broadcast broadcast;
    private int byzantineProcesses;
    private int consensusID;
    private final Service.CurrentConsensus currentConsensus;

    private boolean preparePhase = false;
    private ConcurrentHashMap<Entry<String,Integer>,Block> prepareMessages = new ConcurrentHashMap<>();
    private boolean commitPhase = false;
    private ConcurrentHashMap<Entry<String,Integer>,Block> commitMessages = new ConcurrentHashMap<>();
    private boolean decisionPhase = false;

    //currentRound
    //processRound
    private Block inputValue;
    private Block processValue;
    //private Timer timer = null;

    private Blockchain blockchain;

    public IstanbulBFT(Entry<String,Integer> processID, boolean leader, Entry<String,Integer> leaderID, APL apl, Broadcast broadcast, int byzantineProcesses,
                       Blockchain blockchain, Service.CurrentConsensus currentConsensus) {
        this.processID = processID;
        this.leader = leader;
        this.leaderID = leaderID;
        this.apl = apl;
        this.broadcast = broadcast;
        this.byzantineProcesses = byzantineProcesses;
        this.blockchain = blockchain;
        this.currentConsensus = currentConsensus;
    }

    public synchronized void algorithm1(int consensusCounter, Block block) throws IOException, InterruptedException,
            NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException,
            InvalidKeyException {
        this.inputValue = block;
        this.consensusID = consensusCounter;
        //currentRound

        if (this.leader) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command", "pre-prepare");
            jsonObject.put("consensusID", this.consensusID);
            //currentRound
            jsonObject.put("inputValue", this.inputValue.toJsonObj().toString());
            this.broadcast.doBroadcast(this.inputValue.toJsonObj().toString() + "pre-prepare", jsonObject.toString());
        }
        //timerRound
    }

    public synchronized void algorithm2(String command, Block inputValue, Entry<String,Integer> receivedProcess) throws IOException, InterruptedException,
            NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException {
        if (command.equals("pre-prepare") && this.leaderID.equals(receivedProcess) && !this.preparePhase && !this.commitPhase && !this.decisionPhase) {
            this.preparePhase = true;
            //timerRound
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command", "prepare");
            jsonObject.put("consensusID", this.consensusID);
            //currentRound
            jsonObject.put("inputValue", inputValue.toJsonObj().toString());
            this.broadcast.doBroadcast(inputValue.toJsonObj().toString() + "prepare", jsonObject.toString());
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
                    jsonObject.put("inputValue", inputValue.toJsonObj().toString());
                    this.broadcast.doBroadcast(inputValue.toJsonObj().toString() + "commit", jsonObject.toString());
                }
            }
        }
        else if (command.equals("commit") && !this.commitMessages.containsKey(receivedProcess) && !this.decisionPhase) {
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
                    System.out.println("Istanbul BFT decided: " + inputValue.toJsonObj());

                    //timerRound
                    this.blockchain.addValue(inputValue);
                    System.out.println("============BLOCKCHAIN============");
                    this.blockchain.printBlockchain();

                    synchronized (this.currentConsensus) {
                        this.currentConsensus.id++;
                        this.currentConsensus.notify();
                    }
                }
            }
        }
    }
}
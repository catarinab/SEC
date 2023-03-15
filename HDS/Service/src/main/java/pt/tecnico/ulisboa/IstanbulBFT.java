package pt.tecnico.ulisboa;

import org.json.JSONObject;

import javax.crypto.Mac;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map.Entry;

public class IstanbulBFT {
    private final Entry<String,Integer> processID;
    private boolean processLeader;
    private Broadcast broadcast;
    private int byzantineProcesses;
    private int consensusID;
    private ArrayList<String> prepareMessages = new ArrayList<>();
    private boolean commitPhase = false;
    private ArrayList<String> commitMessages = new ArrayList<>();
    private boolean decisionPhase = false;
    private Blockchain blockchain;

    //currentRound
    //processRound
    private String processValue;
    //private Timer timer = null;

    public IstanbulBFT(Entry<String,Integer> processID, boolean processLeader, Broadcast broadcast, int byzantineProcesses,
                       Blockchain blockchain) throws NoSuchAlgorithmException, InvalidKeyException {
        this.processLeader = processLeader;
        this.processID = processID;
        this.broadcast = broadcast;
        this.byzantineProcesses = byzantineProcesses;
        this.blockchain = blockchain;
    }

    public synchronized void algorithm1(int consensusCounter, String message) throws IOException, InterruptedException {
        this.consensusID = consensusCounter;
        //currentRound

        if (this.processLeader) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command", "pre-prepare");
            jsonObject.put("consensusID", this.consensusID);
            //currentRound
            jsonObject.put("inputValue", message);
            this.broadcast.doBroadcast(message + "pre-prepare", jsonObject.toString());
        }
        //timerRound
    }

    public synchronized void algorithm2(String command, String inputValue) throws IOException, InterruptedException, NoSuchAlgorithmException {
        if (command.equals("pre-prepare")) {
            //timerRound
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command", "prepare");
            jsonObject.put("consensusID", this.consensusID);
            //currentRound
            jsonObject.put("inputValue", inputValue);
            this.broadcast.doBroadcast(inputValue+"prepare", jsonObject.toString());
        }
        else if (command.equals("prepare") && !this.commitPhase) {
            System.out.println("COMMITPHASE="+this.commitPhase);
            this.prepareMessages.add(inputValue);
            System.out.println("pREPARE MESSAGES:"+this.prepareMessages);
            int quorumSize = 2 * this.byzantineProcesses + 1;
            if (this.prepareMessages.size() >= quorumSize) {
                int validCounter = 0;
                for (String message: this.prepareMessages) {
                    if (message.equals(inputValue)) validCounter++;
                }
                System.out.println("Prepare Messages: "+this.prepareMessages);
                System.out.println("Valid Counter"+validCounter);
                if (validCounter >= quorumSize) {
                    System.out.println("inside if, COMMIT PHASE TRUE");
                    this.commitPhase = true;

                    //processRound
                    this.processValue = inputValue;

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("command", "commit");
                    jsonObject.put("consensusID", this.consensusID);
                    //currentRound
                    jsonObject.put("inputValue", inputValue);
                    this.broadcast.doBroadcast(inputValue+"commit", jsonObject.toString());
                }
            }
        }
        else if (command.equals("commit") && !decisionPhase) {
            //timerRound
            this.commitMessages.add(inputValue);

            int quorumSize = 2 * this.byzantineProcesses + 1;
            if (this.commitMessages.size() >= quorumSize) {
                int validCounter = 0;

                for (String message : this.commitMessages) {
                    if (message.equals(inputValue)) validCounter++;
                }
                if (validCounter >= quorumSize) {
                    this.decisionPhase = true;
                    System.out.println("ValidCounter:"+validCounter);
                    System.out.println("DECIDED!:"+inputValue);

                    //timerRound
                    this.blockchain.addValue(inputValue);
                    this.blockchain.printBlockchain();
                }
            }
        }
    }
}
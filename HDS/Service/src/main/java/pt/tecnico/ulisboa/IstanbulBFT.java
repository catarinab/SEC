package pt.tecnico.ulisboa;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.Map.Entry;

public class IstanbulBFT {
    private final Entry<String,Integer> processID;
    private boolean processLeader;
    private Broadcast broadcast;
    //private Timer timer = null;

    public IstanbulBFT(Entry<String,Integer> processID, boolean processLeader, Broadcast broadcast) {
        this.processLeader = processLeader;
        this.processID = processID;
        this.broadcast = broadcast;
    }

    public void algorithm1(int consensusCounter, String message, int messageCounter) throws IOException, InterruptedException {
        int consensusID = consensusCounter;
        //currentRound
        //processRound
        //processValue
        String inputValue = message;

        if (this.processLeader) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("messageID", this.processID.getKey() + String.valueOf(this.processID.getValue()) + String.valueOf(messageCounter));
            jsonObject.put("command", "pre-prepare");
            jsonObject.put("consensusID", consensusID);
            //currentRound
            jsonObject.put("inputValue", inputValue);
            this.broadcast.doBroadcast(jsonObject.toString());
        }
        //timerRound
    }
}
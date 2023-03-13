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
    //private Timer timer = null;

    public IstanbulBFT(Entry<String,Integer> processID, boolean processLeader, Broadcast broadcast) {
        this.processLeader = processLeader;
        this.processID = processID;
        this.broadcast = broadcast;
    }

    public void algorithm1(int consensusCounter, String message, int messageCounter) throws IOException, InterruptedException {
        int consensusID = consensusCounter;
        //currentRound

        if (this.processLeader) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("messageID", this.processID.getKey() + String.valueOf(this.processID.getValue()) +
                            messageCounter);
            jsonObject.put("command", "pre-prepare");
            jsonObject.put("consensusID", consensusID);
            //currentRound
            jsonObject.put("inputValue", message);
            byte[] bytes = message.getBytes();
            byte[] macResult = mac.doFinal(bytes);
            jsonObject.put("mac", Arrays.toString(macResult));
            jsonObject.put("key", Base64.getEncoder().encodeToString(this.serverKey.getEncoded()));
            this.broadcast.doBroadcast(jsonObject.toString());
        }
        //timerRound
    }
}
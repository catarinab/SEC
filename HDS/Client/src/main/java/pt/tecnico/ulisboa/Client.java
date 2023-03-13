package pt.tecnico.ulisboa;

import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map.Entry;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;

public class Client {
    private final Entry<String,Integer> processID;
    private final APL apl;
    private final Broadcast broadcast;
    private int messageCounter = 0;
    private final Mac mac = Mac.getInstance("HmacSHA256");
    private final Key key;
    private static final String RSA = "DES";



    public Client(List<Entry<String,Integer>> processes) throws SocketException, UnknownHostException,
            NoSuchAlgorithmException, InvalidKeyException {
        processID = new AbstractMap.SimpleEntry<>("localhost", 4321);
        this.apl = new APL("localhost", 4321);
        this.broadcast = new Broadcast(processes, this.apl);
        KeyGenerator keyGen = KeyGenerator.getInstance("DES");
        SecureRandom secRandom = new SecureRandom();
        keyGen.init(secRandom);
        this.key = keyGen.generateKey();
        mac.init(key);
    }

    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException,
            InvalidKeyException {
        List<Entry<String,Integer>> processes = Utility.readProcesses("/home/cat/uni/mestrado/SEC/HDS/services.txt").getValue();

        System.out.println(Client.class.getName());
        Client client = new Client(processes);
        client.send("hey pu$$y queen");
    }

    public void send(String message) throws IOException, InterruptedException {
        this.messageCounter++; //substituido por MAC
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("messageID", this.processID.getKey() + this.processID.getValue() + this.messageCounter);
        jsonObject.put("command", "append");
        jsonObject.put("message", message);
        byte[] bytes = message.getBytes();
        byte[] macResult = mac.doFinal(bytes);
        jsonObject.put("mac", Arrays.toString(macResult));
        jsonObject.put("key", Base64.getEncoder().encodeToString(this.key.getEncoded()));
        this.broadcast.doBroadcast(jsonObject.toString());
    }

}
package pt.tecnico.ulisboa;

import org.json.JSONObject;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

//Por enquanto fica apenas PL.
public class APL {
    private final StubbornLink stubbornLink;
    private final Key key;

    private final Mac mac = Mac.getInstance("HmacSHA256");

    public APL(String hostname, int port, ConcurrentHashMap<String, JSONObject> acksReceived) throws
            SocketException, UnknownHostException, NoSuchAlgorithmException, InvalidKeyException {
        this.stubbornLink = new StubbornLink(hostname, port, 10, 1, acksReceived);
        KeyGenerator keyGen = KeyGenerator.getInstance("DES");
        SecureRandom secRandom = new SecureRandom();
        keyGen.init(secRandom);
        this.key = keyGen.generateKey();
        mac.init(this.key);
    }

    public void send(String inputValue, String message, String hostName, int port) throws IOException, InterruptedException {
        JSONObject jsonToSend = new JSONObject(message);
        byte[] macResult = mac.doFinal(inputValue.getBytes());
        jsonToSend.put("mac", Arrays.toString(macResult));
        jsonToSend.put("key", Base64.getEncoder().encodeToString(this.key.getEncoded()));
        this.stubbornLink.send(jsonToSend.toString(), hostName, port);
    }

    public String receive() throws IOException {
        String received = this.stubbornLink.receive();
        JSONObject message = new JSONObject(received);
        String command = message.getString("command");
        if(command.equals("ack")) return received;
        try {
            String keyBase64 = message.getString("key");
            String macResultMessage = message.getString("mac");
            String messageContent = message.getString("inputValue");
            byte[] encodedKey = Base64.getDecoder().decode(keyBase64);
            Key receivedKey = new SecretKeySpec(encodedKey,0,encodedKey.length, "DES");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(receivedKey);
            byte[] macResult = mac.doFinal((messageContent+command).getBytes());
            if(!macResultMessage.equals(Arrays.toString((macResult)))){
                System.out.println("mac errado");
                return;
            }
        } catch (Exception e) {
            System.out.println("mac errado");
            System.out.println(received);
            throw new RuntimeException(e);
        }
        return received;
    }
}

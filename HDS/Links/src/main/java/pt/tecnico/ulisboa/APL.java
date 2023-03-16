package pt.tecnico.ulisboa;

import org.json.JSONObject;

import javax.crypto.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.UnknownHostException;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

//Por enquanto fica apenas PL.
public class APL {
    private final StubbornLink stubbornLink;
    private final PublicKey publicKey;
    private final PrivateKey privateKey;
    private final MessageDigest digest = MessageDigest.getInstance("SHA-256");
    public String hostname;
    public int port;

    public APL(String hostname, int port, ConcurrentHashMap<String, JSONObject> acksReceived) throws
            IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        this.stubbornLink = new StubbornLink(hostname, port, 10, 1, acksReceived);
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
        //now, we save the private key in a file, to emulate a PKI
        FileOutputStream keyStream = new FileOutputStream("../"+hostname+","+port+"key.txt");
        keyStream.write(Base64.getEncoder().encodeToString(this.publicKey.getEncoded()).getBytes());
        keyStream.close();
        this.hostname = hostname;
        this.port = port;
    }

    public void send(String inputValue, String message, String hostName, int port) throws IOException,
            InterruptedException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        JSONObject jsonToSend = new JSONObject(message);
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, this.privateKey);
        byte[] macResult = digest.digest(inputValue.getBytes());
        byte[] encryptedMac = encryptCipher.doFinal(macResult);
        jsonToSend.put("mac", Base64.getEncoder().encodeToString(encryptedMac));
        jsonToSend.put("key", Base64.getEncoder().encodeToString(this.publicKey.getEncoded()));
        jsonToSend.put("hostname", this.hostname);
        jsonToSend.put("port", this.port);
        this.stubbornLink.send(jsonToSend.toString(), hostName, port);
    }

    public String receive() throws IOException {
        String received = this.stubbornLink.receive();
        JSONObject message = new JSONObject(received);
        String command = message.getString("command");
        if(command.equals("ack")) return received;
        try {
            String keyBase64 = message.getString("key");
            String encryptedMacB64 = message.getString("mac");
            String messageContent = message.getString("inputValue");
            String receivedHostname = message.getString("hostname");
            int receivedPort = message.getInt("port");
            BufferedReader keyStream = new BufferedReader(new InputStreamReader(new FileInputStream
                    ("../"+receivedHostname+","+receivedPort+"key.txt"), StandardCharsets.UTF_8));
            String PKIKeyBase64 = keyStream.readLine();
            keyStream.close();
            if (!PKIKeyBase64.equals(keyBase64)) {
                System.out.println("Wrong KEY!");
                System.out.println(message);
                System.out.println(PKIKeyBase64);
                System.out.println(keyBase64);
                throw new RuntimeException();
            }
            byte[] encodedKey = Base64.getDecoder().decode(keyBase64);
            PublicKey receivedKey = KeyFactory.getInstance("RSA")
                                    .generatePublic(new X509EncodedKeySpec(encodedKey));
            Cipher decryptCipher = Cipher.getInstance("RSA");
            decryptCipher.init(Cipher.DECRYPT_MODE, receivedKey);

            byte[] encryptedMac = Base64.getDecoder().decode(encryptedMacB64);
            byte[] decryptedMacReceived = decryptCipher.doFinal(encryptedMac);
            byte[] macResult = digest.digest((messageContent+command).getBytes());
            if (!Arrays.toString(decryptedMacReceived).equals(Arrays.toString(macResult))) {
                System.out.println("Wrong MAC.");
                System.out.println(message);
                System.out.println(Arrays.toString(decryptedMacReceived));
                System.out.println(Arrays.toString(macResult));
                throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return received;
    }
}

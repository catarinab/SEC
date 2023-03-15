package pt.tecnico.ulisboa;

import org.json.JSONObject;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
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

    public APL(String hostname, int port, ConcurrentHashMap<String, JSONObject> acksReceived) throws
            SocketException, UnknownHostException, NoSuchAlgorithmException {
        this.stubbornLink = new StubbornLink(hostname, port, 10, 1, acksReceived);
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
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

            byte[] encodedKey = Base64.getDecoder().decode(keyBase64);
            PublicKey receivedKey = KeyFactory.getInstance("RSA")
                                    .generatePublic(new X509EncodedKeySpec(encodedKey));
            Cipher decryptCipher = Cipher.getInstance("RSA");
            decryptCipher.init(Cipher.DECRYPT_MODE, receivedKey);

            byte[] encryptedMac = Base64.getDecoder().decode(encryptedMacB64);
            byte[] decryptedMacReceived = decryptCipher.doFinal(encryptedMac);
            byte[] macResult = digest.digest((messageContent+command).getBytes());
            if(!Arrays.toString(decryptedMacReceived).equals(Arrays.toString(macResult))){
                System.out.println("Wrong MAC.");
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

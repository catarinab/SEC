package pt.tecnico.ulisboa;

import org.json.JSONObject;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.Mac;

public class Service extends Thread {
    private final Entry<String,Integer> processID;
    private final APL apl;
    private final Broadcast broadcast;
    private boolean leader = false;
    private ArrayList<String> delivered = new ArrayList<>();
    private int messageCounter = 0;
    private int byzantineProcesses = 0;
    private ConcurrentHashMap<Integer, IstanbulBFT> consensusInstances = new ConcurrentHashMap<>();
    private int consensusCounter = 0;
    private final Mac mac = Mac.getInstance("HmacSHA256");
    private final Key serverKey;
    private static final String RSA = "DES";

    private JSONObject message = null;

    public Service(String hostname, int port, int byzantineProcesses, List<Entry<String,Integer>> processes, boolean leader)
            throws SocketException, UnknownHostException, NoSuchAlgorithmException, InvalidKeyException {
        processID = new AbstractMap.SimpleEntry<>(hostname, port);
        this.apl = new APL(hostname, port);
        this.leader = leader;
        this.broadcast = new Broadcast(processes, this.apl);
        KeyGenerator keyGen = KeyGenerator.getInstance("DES");
        SecureRandom secRandom = new SecureRandom();
        keyGen.init(secRandom);
        this.serverKey = keyGen.generateKey();
        mac.init(serverKey);
    }

    public Service(Service father, JSONObject message) throws NoSuchAlgorithmException, InvalidKeyException {
        this.processID = father.processID;
        this.apl = father.apl;
        this.broadcast = father.broadcast;
        this.leader = father.leader;
        this.delivered = father.delivered;
        this.messageCounter = father.messageCounter;
        this.byzantineProcesses = father.byzantineProcesses;
        this.consensusInstances = father.consensusInstances;
        this.consensusCounter = father.consensusCounter;
        this.message = message;
        this.serverKey = father.serverKey;
        this.mac.init(this.serverKey);
    }


    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException,
            InvalidKeyException {
        String hostname = null;
        int port = 0;
        if(args.length != 2) serviceUsage();
        try {
            hostname = args[0];
            port = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException nfe) {
            serviceUsage();
        }

        Entry<Integer, List<Entry<String,Integer>>> fileSetup = Utility.readProcesses("/home/cat/uni/mestrado/SEC/HDS/services.txt");
        int byzantineProcesses = fileSetup.getKey();
        List<Entry<String,Integer>> processes = fileSetup.getValue();
        boolean leader = false;
        for (int i = 0; i < processes.size(); i++) {
            if (processes.get(i).getKey().equals(hostname) && processes.get(i).getValue() == port) {
                if (i == 0) leader = true;
                processes.remove(i);
                break;
            }
        }

        Service service = new Service(hostname, port, byzantineProcesses, processes, leader);
        System.out.println(Service.class.getName());
        while(true) service.receive();
    }

    public static void serviceUsage() {
        System.out.println("Usage: Service port");
        System.out.println("port is an int with a maximum of 5 chars");
        System.exit(1);
    }

    public void receive() throws IOException {
        String message = this.apl.receive();

        System.out.println(message);

        try {
            JSONObject jsonObject = new JSONObject(message);
            if(jsonObject.getString("command").equals("ack")) return;
            String macResultMessage = jsonObject.getString("mac");
            if (!delivered.contains(macResultMessage)) {
                delivered.add(macResultMessage);
            }
            else return;
            Service thread = new Service(this, jsonObject);
            thread.start();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }

    public void run() {
        System.out.println("This code is running in a thread with message: " + this.message);
        String command = this.message.getString("command");
        //if(command.equals("ack")) return;
        if (command.equals("append")) {
            String keyBase64 = this.message.getString("key");
            String macResultMessage = this.message.getString("mac");
            String messageContent = this.message.getString("message");
            byte[] encodedKey = Base64.getDecoder().decode(keyBase64);
            Key clientKey = new SecretKeySpec(encodedKey,0,encodedKey.length, "DES");
            byte[] bytes = messageContent.getBytes();
            try {
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(clientKey);
                byte[] macResult = mac.doFinal(bytes);
                if(!macResultMessage.equals(Arrays.toString((macResult)))) return;
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }

            IstanbulBFT istanbulBFT = null;
            try {
                istanbulBFT = new IstanbulBFT(this.processID, this.leader, this.broadcast,
                        this.byzantineProcesses, this.serverKey);
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
            this.messageCounter++;
            this.consensusCounter++;
            try {
                istanbulBFT.algorithm1(this.consensusCounter, this.message.getString("message"), this.messageCounter);
                consensusInstances.put(consensusCounter, istanbulBFT);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (command.equals("pre-prepare") || command.equals("prepare") || command.equals("commit")) {
            this.messageCounter++;
            try {
                System.out.println(this.message);
                System.out.println(this.consensusInstances);
                this.consensusInstances.get(this.message.getInt("consensusID")).algorithm2(command, this.message.getString("inputValue"), this.messageCounter);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
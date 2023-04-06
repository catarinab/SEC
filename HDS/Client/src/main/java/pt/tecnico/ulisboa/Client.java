package pt.tecnico.ulisboa;

import org.json.JSONObject;

import java.io.IOException;
import java.security.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Scanner;

import javax.crypto.*;

public class Client extends Thread{
    private final Entry<String,Integer> processID;
    private final int numProcesses;
    private final List<Entry<String,Integer>> processes;
    private final int byzantineProcesses;
    private ConcurrentHashMap<String,ConcurrentHashMap<Entry<String,Integer>, String>> messagesReceived = new ConcurrentHashMap<>();
    private final List<String> messagesHandled;
    private final Broadcast broadcast;
    private final APL apl;
    private ConcurrentHashMap<String, JSONObject> acksReceived = new ConcurrentHashMap<>();
    private final Object quorumLock = new Object();


    public Client(String hostname, int port, List<Entry<String,Integer>> processes, int byzantineProcesses) throws IOException,
            NoSuchAlgorithmException {
        this.processID = new AbstractMap.SimpleEntry<>(hostname, port);
        this.numProcesses = processes.size();
        this.processes = processes;
        this.byzantineProcesses = byzantineProcesses;
        this.apl = new APL(hostname, port, acksReceived);
        this.broadcast = new Broadcast(processes, apl);
        this.messagesHandled = new java.util.ArrayList<>();
    }

    public Client(Client client) {
        this.processID = client.processID;
        this.numProcesses = client.numProcesses;
        this.processes = client.processes;
        this.byzantineProcesses = client.byzantineProcesses;
        this.messagesReceived = client.messagesReceived;
        this.messagesHandled = client.messagesHandled;
        this.apl = client.apl;
        this.broadcast = client.broadcast;
        this.acksReceived = client.acksReceived;

    }

    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException,
            InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {

        System.out.println(Client.class.getName());

        String hostname = System.getProperty("hostname");
        int port = Integer.parseInt(System.getProperty("port"));
        String path = System.getProperty("path");

        Entry<Integer, List<Entry<String,Integer>>> fileSetup = Utility.readProcesses(path);
        int byzantineProcesses = fileSetup.getKey();
        List<Entry<String,Integer>> processes = fileSetup.getValue();

        Client client = new Client(hostname, port, processes, byzantineProcesses);
        Client thread = new Client(client);
        thread.start();
        System.out.println("Possible commands: ");
        System.out.println("create_account (creates an account for this client with its public key)");
        System.out.println("check_balance option (checks the balance of the clients account, if there is one, option" +
                " should be \"weak\" or \"strong\") ");
        System.out.println("transfer amount DestinationPublicKey (transfers amount to the account with the public Key " +
                "DestinationPublicKey)");
        // Loop to read messages from the user and send them to the server
        while (true) {
            Scanner input = new Scanner(System.in);
            System.out.print("Insert the command you wish to execute:");
            String message = input.nextLine();
            String[] splitMessage = message.split(" ");
            String command = splitMessage[0];
            switch (command) {
                case "create_account":
                    client.send(command, "");
                    break;
                case "transfer":
                    try {
                        Integer.parseInt(splitMessage[1]);
                        if(splitMessage.length != 3) throw new RuntimeException("wrong command specification");
                        client.send(command, splitMessage[1] + ";" + splitMessage[2]);
                    } catch (Exception e) {
                        System.out.println("transfer amount DestinationPublicKey (transfers amount to the account with the "
                                + "public Key DestinationPublicKey)");
                        System.out.println("amount should be an Integer");
                    }
                    break;
                case "check_balance":
                    try{
                        if (!splitMessage[1].equals("weak") && !splitMessage[1].equals("strong"))
                            throw new RuntimeException("wrong command specification");
                        client.send(command, splitMessage[1]);
                    }
                    catch (Exception e) {
                        System.out.println("check_balance option");
                        System.out.println("option should be \"weak\" for Weakly Consistent Read \nOR\n " +
                                "\"strong\" for Strongly Consistent Read");
                    }

                    break;
                default:
                    System.out.println("Command not permitted.");
                    break;
            }

        }
    }

    public void send(String command, String message) throws IOException, InterruptedException, NoSuchPaddingException,
            IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command", command);
        jsonObject.put("inputValue", message);
        if(command.equals("transfer")) {
            String[] splitMessage = message.split(";");
            jsonObject.put("amount", splitMessage[0]);
            jsonObject.put("destination", splitMessage[1]);
        }
        this.broadcast.doBroadcast(message + command, jsonObject.toString());
    }

    public boolean quorumReplies(String inputValue, String digSignature, String receivedHostname, int receivedPort) {
        synchronized (this.quorumLock) {
            if (!this.messagesHandled.contains(digSignature)) {
                Entry<String, Integer> receivedProcess = null;
                for (Entry<String, Integer> process : this.processes) {
                    if (process.getKey().equals(receivedHostname) && process.getValue() == receivedPort) {
                        receivedProcess = process;
                        break;
                    }
                }
                if(receivedProcess == null) throw new RuntimeException("process not found"+receivedHostname+receivedPort);
                if (!this.messagesReceived.containsKey(digSignature)) {
                    this.messagesReceived.put(digSignature, new ConcurrentHashMap<>());
                    this.messagesReceived.get(digSignature).put(receivedProcess, inputValue);
                } else if (!this.messagesReceived.get(digSignature).containsKey(receivedProcess)) {
                    this.messagesReceived.get(digSignature).put(receivedProcess, inputValue);
                    int quorumSize = 2 * this.byzantineProcesses + 1;
                    if (this.messagesReceived.get(digSignature).size() >= quorumSize) {
                        int validCounter = 0;

                        for (Entry<String, Integer> key : this.messagesReceived.get(digSignature).keySet()) {
                            if (this.messagesReceived.get(digSignature).get(key).equals(inputValue)) validCounter++;
                        }
                        if (validCounter >= quorumSize) {
                            this.messagesReceived.remove(digSignature);
                            this.messagesHandled.add(digSignature);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void receive() throws IOException {
        String message = this.apl.receive();
        JSONObject jsonObject = new JSONObject(message);
        String messageID = jsonObject.getString("mac");
        String command = jsonObject.getString("command");
        if (command.equals("ack")) acksReceived.put(messageID, jsonObject);
        else if (jsonObject.getString("command").equals("error")) {
            String inputValue = jsonObject.getString("inputValue");
            String digSignature = jsonObject.getString("digSignature");
            String receivedHostname = jsonObject.getString("hostname");
            int receivedPort = jsonObject.getInt("port");
            if (this.quorumReplies(inputValue, digSignature, receivedHostname, receivedPort)) {
                System.out.println("There was an error with the request: " + inputValue);
            }
        }
        else if (command.equals("decide")) {
            String inputValue = jsonObject.getString("inputValue");
            OperationDTO operationDTO;
            JSONObject operation = new JSONObject(inputValue);
            String type = operation.getString("transaction");
            if(type.equals("createAcc")) operationDTO = new CreateAccDTO(operation);
            else if(type.equals("transfer")) operationDTO = new TransferDTO(operation);
            else throw new RuntimeException();
            String digSignature = operationDTO.getDigSignature();
            String receivedHostname = jsonObject.getString("hostname");
            int receivedPort = jsonObject.getInt("port");
            if (this.quorumReplies(inputValue, digSignature, receivedHostname, receivedPort)) {
                System.out.println("\nThe operation \"" + operationDTO.toString() + "\" has been appended to the blockchain.");
            }
        }
    }

    public void run() {
        while (true) {
            try {
                receive();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
package pt.tecnico.ulisboa;

import org.json.JSONObject;

import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import pt.tecnico.ulisboa.Utility;

public class Service extends Thread {
    private Entry<String,Integer> processID;
    private APL apl;
    private Broadcast broadcast;
    private boolean leader = false;
    private ArrayList<String> delivered = new ArrayList<>();
    private int messageCounter = 0;
    private int consensusCounter = 0;

    private JSONObject message = null;

    public Service(String hostname, int port, List<Entry<String,Integer>> processes, boolean leader) throws SocketException, UnknownHostException {
        processID = new AbstractMap.SimpleEntry<>(hostname, port);
        this.apl = new APL(hostname, port);
        this.leader = leader;
        this.broadcast = new Broadcast(processes, this.apl);
    }

    public Service(Entry<String,Integer> processID, APL apl, Broadcast broadcast, boolean leader, ArrayList<String> delivered, int messageCounter, int consensusCounter, JSONObject message) {
        this.processID = processID;
        this.apl = apl;
        this.broadcast = broadcast;
        this.leader = leader;
        this.delivered = delivered;
        this.messageCounter = messageCounter;
        this.consensusCounter = consensusCounter;
        this.message = message;
    }


    public static void main(String[] args) throws IOException, InterruptedException {
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

        List<Entry<String,Integer>> processes = Utility.readProcesses("/home/rita/SEC/HDS/services.txt");
        boolean leader = false;
        for (int i = 0; i < processes.size(); i++) {
            if (processes.get(i).getKey().equals(hostname) && processes.get(i).getValue() == port) {
                if (i == 0) leader = true;
                processes.remove(i);
                break;
            }
        }

        Service service = new Service(hostname, port, processes, leader);
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
        if(message.equals("ack")) return;

        try {
            JSONObject jsonObject = new JSONObject(message);
            String messageID = jsonObject.getString("messageID");
            if (!delivered.contains(messageID)) {
                delivered.add(messageID);
            }
            else return;
            Service thread = new Service(this.processID, this.apl, this.broadcast, this.leader, this.delivered, this.messageCounter, this.consensusCounter, jsonObject);
            thread.start();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }

    public void run() {
        System.out.println("This code is running in a thread with message: " + this.message);
        if (this.message.getString("command").equals("append")) {
            IstanbulBFT istanbulBFT = new IstanbulBFT(this.processID, this.leader, this.broadcast);
            this.messageCounter++;
            this.consensusCounter++;
            try {
                istanbulBFT.algorithm1(this.consensusCounter, this.message.getString("message"), this.messageCounter);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
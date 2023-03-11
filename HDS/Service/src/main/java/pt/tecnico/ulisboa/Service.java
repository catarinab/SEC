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

public class Service extends Thread {
    private APL apl;
    private String command = null;
    private String message = null;
    private Broadcast broadcast;
    private boolean leader = false;
    private ArrayList<String> delivered = new ArrayList<>();

    public Service(String hostname, int port, List<Entry<String,Integer>> processes, boolean leader) throws SocketException, UnknownHostException {
        this.apl = new APL(hostname, port, Utility.Type.SERVER);
        this.leader = leader;
        this.broadcast = new Broadcast(new AbstractMap.SimpleEntry<>(hostname, port),
                                        processes, this.apl);
    }

    public Service(String command, String message) {
        this.command = command;
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

        List<Entry<String,Integer>> processes= new java.util.ArrayList<>();
        boolean leader = false;
        try {
            File file = new File("/home/rita/SEC/HDS/processes.txt");
            Scanner scanner = new Scanner(file);
            int lineCounter = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] splited = line.split("\\s+");
                String processHostname = splited[0];
                int processPort = Integer.parseInt(splited[1]);
                if (processHostname.equals(hostname) && processPort == port) {
                    if (lineCounter == 0) leader = true;
                    continue;
                }
                processes.add(new AbstractMap.SimpleEntry<>(processHostname, processPort));
                lineCounter++;
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("File does not exist.");
            e.printStackTrace();
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
            Service thread = new Service(jsonObject.getString("command"), jsonObject.getString("message"));
            thread.start();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }

    public void run() {
        System.out.println("This code is running in a thread with message: " + this.message);
    }
}
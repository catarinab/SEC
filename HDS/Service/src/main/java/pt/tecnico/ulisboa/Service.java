package pt.tecnico.ulisboa;

import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class Service extends Thread {
    private APL apl;
    private String message = null;
    private Broadcast broadcast;
    private boolean leader = false;
    private ArrayList<String> delivered = new ArrayList<>();

    public Service(int port) throws SocketException, UnknownHostException {
        this.apl = new APL("localhost", port, Utility.Type.SERVER);
        if(port == 1234) this.leader = true;
        List<Entry<String,Integer>> processes= new java.util.ArrayList<>();
        processes.add(new AbstractMap.SimpleEntry<>("localhost", 1234));
        processes.add(new AbstractMap.SimpleEntry<>("localhost", 1235));
        processes.add(new AbstractMap.SimpleEntry<>("localhost", 1236));
        this.broadcast = new Broadcast(new AbstractMap.SimpleEntry<>("localhost", port),
                                        processes, this.apl);
    }

    public Service(String message) {
        this.message = message;
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 0;
        if(args.length != 1) serviceUsage();
        try {
            port = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException nfe) {
            serviceUsage();
        }
        Service service = new Service(port);
        System.out.println(Service.class.getName());
        while(true) service.receive();
    }

    public static void serviceUsage() {
        System.out.println("Usage: Service port");
        System.out.println("port is an int with a maximum of 5 chars");
        System.exit(1);
    }

    public void receive() throws IOException {
        String command = null;
        String message = this.apl.receive();
        System.out.println(message);
        /*
        try {
            JSONObject jsonObject = new JSONObject(message);
            String key = jsonObject.keys().next();
            if (key.equals("messageID")) {
                String messageID = jsonObject.getString("messageID");
                if (!delivered.contains(messageID)) {
                    delivered.add(messageID);
                    command = jsonObject.getString("append");
                }
            }
            else if (key.equals("serverID")) {

            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        if (command == null) return;
        Service thread = new Service(command);
        thread.start();
        */

    }

    public void run() {
        System.out.println("This code is running in a thread with message: " + this.message);
    }
}
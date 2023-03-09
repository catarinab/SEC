package pt.tecnico.ulisboa;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Service extends Thread {
    private final APL apl;

    public Service(int port) throws SocketException, UnknownHostException {
        this.port = port;
        this.apl = new APL("localhost", this.port, Utility.Type.SERVER);
    }

    public static void main(String[] args) throws IOException {
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
        String message = this.apl.receive();
        if (message == null) return;
        System.out.println(message);
        Service thread = new Service();
        thread.start();
    }

    public void run() {
        System.out.println("This code is running in a thread");
    }
}
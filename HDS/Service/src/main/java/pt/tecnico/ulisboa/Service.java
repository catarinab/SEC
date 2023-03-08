package pt.tecnico.ulisboa;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.management.StringValueExp;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Service extends Thread {
    private final APL apl;

    public Service() throws SocketException, UnknownHostException {
        this.apl = new APL("localhost", 1234, Utility.Type.SERVER);
    }

    public static void main(String[] args) throws IOException {
        System.out.println(Service.class.getName());
        Service service = new Service();
        while(true) service.receive();
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
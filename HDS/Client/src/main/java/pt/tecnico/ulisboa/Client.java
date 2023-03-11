package pt.tecnico.ulisboa;

import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client {

    private final APL apl;
    private int messageCounter = 0;

    public Client() throws SocketException, UnknownHostException {
        this.apl = new APL("localhost", 1234, Utility.Type.CLIENT);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println(Client.class.getName());
        Client client = new Client();
        client.send("ola");
    }

    public void send(String message) throws IOException, InterruptedException {
        this.messageCounter++; //substituido por mac
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("messageID", String.valueOf(this.messageCounter));
        jsonObject.put("command", "append");
        jsonObject.put("message", message);
        if(this.apl.send(jsonObject.toString())) {
            System.out.println("recebeu ack");
        }
        else{
            System.out.println("nao recebeu ack");
        }

    }

}
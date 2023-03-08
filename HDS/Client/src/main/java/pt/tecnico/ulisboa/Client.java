package pt.tecnico.ulisboa;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client {

    private final APL apl;

    public Client() throws SocketException, UnknownHostException {
        this.apl = new APL("localhost", 1234, Utility.Type.CLIENT);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println(Client.class.getName());
        Client client = new Client();
        client.send("ola");
    }

    public void send(String message) throws IOException, InterruptedException {
        if(this.apl.send(message)){
            System.out.println("recebeu ack");
        }
        else{
            System.out.println("nao recebeu ack");
        }

    }

}
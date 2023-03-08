package pt.tecnico.ulisboa;

import java.io.IOException;
import java.net.*;

public class Client {

    private FLLSender FLLSender = null;

    public Client() throws SocketException, UnknownHostException {
        this.FLLSender = new FLLSender("localhost", 1234);
    }

    public void main(String[] args) throws IOException {
        System.out.println(Client.class.getName());
        this.send("append");
    }

    public void send(String message) throws IOException {
        this.FLLSender.send(message.getBytes());
    }

}
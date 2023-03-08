package pt.tecnico.ulisboa;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import pt.tecnico.ulisboa.FLLReceiver;
// ig que tem de usar stubborn links
public class Service {

    private final FLLReceiver fllReceiver;
    public Service() throws SocketException, UnknownHostException {
        this.fllReceiver = new FLLReceiver("localhost", 1234);
    }

    public static void main(String[] args) throws IOException {
        System.out.println(Service.class.getName());
    }

    public void receive() throws IOException {
        String message = this.fllReceiver.receive();

        System.out.println("ola!");

        if (message.equals("append")) {
            System.out.println("Client sent append");
        }
    }}
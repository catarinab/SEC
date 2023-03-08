package pt.tecnico.ulisboa;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class StubbornLink {
    private final FLLSender fllSender;
    private final FLLReceiver fllReceiver;
    private int maxAttempts;
    private int maxDelay;

    public StubbornLink(String hostname, int port) throws SocketException, UnknownHostException {
        this.fllSender = new FLLSender(hostname, port);
        this.fllReceiver = new FLLReceiver(hostname, port);
    }

    public boolean send(String message) throws IOException, InterruptedException {
        boolean ack = false;
        for(int attempts = 0; attempts < maxAttempts && !ack; attempts++) {
            this.fllSender.send(message.getBytes());
            TimeUnit.SECONDS.sleep(maxDelay);
            ack = (this.fllReceiver.receive().equals("ack"));
        }
        return ack;
    }

    public String receive() throws IOException, InterruptedException {
        String message = this.fllReceiver.receive();
        if(message.length() != 0){
            this.fllSender.send("ack".getBytes());
            return message;
        }
        return null;
    }
}

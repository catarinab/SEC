package pt.tecnico.ulisboa;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class StubbornLink {
    private FLLSender fllSender;
    private FLLReceiver fllReceiver;
    private final int maxAttempts;
    private final int maxDelay;

    public StubbornLink(String hostname, int port, int maxAttempts, int maxDelay, Utility.Type type) throws SocketException, UnknownHostException {
        this.maxAttempts = maxAttempts;
        this.maxDelay = maxDelay;
        switch(type) {
            case CLIENT:
                this.fllSender = new FLLSender(hostname, port);
                break;
            case SERVER:
                this.fllReceiver = new FLLReceiver(hostname, port);
                break;
        }
    }

    public boolean send(String message) throws IOException, InterruptedException {
        boolean ack = false;
        for (int attempts = 0; attempts < this.maxAttempts && !ack; attempts++) {
            ack = (this.fllSender.send(message.getBytes()).equals("ack"));
            if(!ack){
                System.out.println("nao deu ack");
                TimeUnit.SECONDS.sleep(this.maxDelay);
            }
        }
        return ack;
    }

    public String receive() throws IOException {
        return this.fllReceiver.receive();
    }
}

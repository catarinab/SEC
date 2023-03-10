package pt.tecnico.ulisboa;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class StubbornLink {
    private FLL fll;
    private final int maxAttempts;
    private final int maxDelay;
    private Utility.Type type;

    public StubbornLink(String hostname, int port, int maxAttempts, int maxDelay, Utility.Type type)
            throws SocketException, UnknownHostException {
        this.maxAttempts = maxAttempts;
        this.maxDelay = maxDelay;
        this.type = type;
        switch(type) {
            case CLIENT:
                this.fll = new FLLSender(hostname, port);
                break;
            case SERVER:
                this.fll = new FLLReceiver(hostname, port);
                break;
        }
    }

    public boolean send(String message) throws IOException, InterruptedException {
        if(this.type == Utility.Type.SERVER) return false;
        for (int attempts = 0; attempts < this.maxAttempts; attempts++) {
            if(this.fll.send(message.getBytes())) {
                return true;
            }
            System.out.println("nao deu ack");
            TimeUnit.SECONDS.sleep(this.maxDelay);
        }
        return false;
    }

    public boolean send(String message, String hostName, int port) throws IOException, InterruptedException {
        if(this.type == Utility.Type.CLIENT) return false;
        for (int attempts = 0; attempts < this.maxAttempts; attempts++) {
            if(this.fll.send(message.getBytes(), hostName, port)) {
                return true;
            }
            System.out.println("nao deu ack");
            TimeUnit.SECONDS.sleep(this.maxDelay);
        }
        return false;
    }

    public String receive() throws IOException {
        return this.fll.receive();
    }
}

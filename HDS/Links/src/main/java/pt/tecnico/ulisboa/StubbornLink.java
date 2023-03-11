package pt.tecnico.ulisboa;

import jdk.jshell.execution.Util;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class StubbornLink {
    private final FLL fll;
    private final int maxAttempts;
    private final int maxDelay;

    public StubbornLink(String hostname, int port, int maxAttempts, int maxDelay, Utility.Type type)
            throws SocketException, UnknownHostException {
        this.maxAttempts = maxAttempts;
        this.maxDelay = maxDelay;
        this.fll = new FLL(hostname, port, type);
    }

    //as funcoes podem ser juntas

    public boolean send(String message) throws IOException, InterruptedException {
        for (int attempts = 0; attempts < this.maxAttempts; attempts++) {
            if(this.fll.send(message.getBytes())) return true;
            System.out.println("nao deu ack");
            TimeUnit.SECONDS.sleep(this.maxDelay);
        }
        return false;
    }

    public void send(String message, String hostName, int port) throws IOException, InterruptedException {
        for (int attempts = 0; attempts < this.maxAttempts; attempts++) {
            if(this.fll.send(message.getBytes(), hostName, port)) return;
            System.out.println("nao deu ack");
            TimeUnit.SECONDS.sleep(this.maxDelay);
        }
    }

    public String receive() throws IOException {
        return this.fll.receive();
    }
}

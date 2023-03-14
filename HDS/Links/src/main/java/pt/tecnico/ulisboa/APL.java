package pt.tecnico.ulisboa;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

//Por enquanto fica apenas PL.
public class APL {
    private final StubbornLink stubbornLink;

    public APL(String hostname, int port, ConcurrentHashMap<String, JSONObject> acksReceived) throws SocketException, UnknownHostException {
        this.stubbornLink = new StubbornLink(hostname, port, 10, 1, acksReceived);
    }

    public void send(String message, String hostName, int port) throws IOException, InterruptedException {
        this.stubbornLink.send(message, hostName, port);
    }

    public String receive() throws IOException {
        return this.stubbornLink.receive();
    }
}

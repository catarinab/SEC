package pt.tecnico.ulisboa;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import pt.tecnico.ulisboa.Utility.Type;

//Por enquanto fica apenas PL.
public class APL {
    private final StubbornLink stubbornLink;
    private final Utility.Type type;

    public APL(String hostname, int port, Type type) throws SocketException, UnknownHostException {
        this.stubbornLink = new StubbornLink(hostname, port, 10, 2, type);
        this.type = type;
    }

    public boolean send(String message) throws IOException, InterruptedException {
        return this.stubbornLink.send(message);
    }

    public void send(String message, String hostName, int port) throws IOException, InterruptedException {
        if(this.type != Utility.Type.SERVER) return;
        this.stubbornLink.send(message, hostName, port);
    }

    public String receive() throws IOException {
        return this.stubbornLink.receive();
    }
}

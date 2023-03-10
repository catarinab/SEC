package pt.tecnico.ulisboa;

import java.io.IOException;

public interface FLL {

    public boolean send(byte[] message) throws IOException;

    //send messages to servers
    boolean send(byte[] message, String hostname, int port) throws IOException;

    public String receive() throws IOException;

}

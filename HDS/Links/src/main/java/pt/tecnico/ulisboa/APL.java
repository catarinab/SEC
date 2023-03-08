package pt.tecnico.ulisboa;

import java.net.SocketException;
import java.net.UnknownHostException;

public class APL {
    private final StubbornLink stubbornLink;
    private int delivered[];

    public APL(String hostname, int port) throws SocketException, UnknownHostException {
        this.stubbornLink = new StubbornLink(hostname, port);
    }
}

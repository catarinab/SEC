package pt.tecnico.ulisboa;
import java.io.IOException;
import java.net.*;

public class FLLSender {
    private final DatagramSocket ds;
    private final InetAddress address;
    private final int port;
    public FLLSender(String hostName, int port) throws SocketException, UnknownHostException {
        this.ds = new DatagramSocket();
        this.address = InetAddress.getByName(hostName);
        this.port = port;
    }

    public static void main(String[] args) {
        System.out.println(FLLSender.class.getName());
    }

    public void send(byte[] message) throws IOException {
        while (true) {

            DatagramPacket DpSend = new DatagramPacket(message, message.length, this.address, this.port);

            ds.send(DpSend);
        }
    }
}
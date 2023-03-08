package pt.tecnico.ulisboa;
import java.io.IOException;
import java.net.*;

public class FLLReceiver {
    private final DatagramSocket ds;
    private final InetAddress address;
    private final int port;
    public FLLReceiver(String hostName, int port) throws SocketException, UnknownHostException {
        this.ds = new DatagramSocket(port);
        this.address = InetAddress.getByName(hostName);
        this.port = port;
    }

    public static void main(String[] args) {
        System.out.println(FLLReceiver.class.getName());
    }

    public String receive() throws IOException {
            byte[] receive = new byte[65535];

            DatagramPacket DpReceive = new DatagramPacket(receive, receive.length);

            this.ds.receive(DpReceive);

            System.out.println("Client:-" + data(receive));
            return data(receive).toString();
    }

    public static StringBuilder data(byte[] a) {
        if (a == null) return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0) {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }
}
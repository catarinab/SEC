package pt.tecnico.ulisboa;
import java.io.IOException;
import java.net.*;

public class FLLReceiver {
    private final DatagramSocket ds;
    public FLLReceiver(String hostName, int port) throws SocketException, UnknownHostException {
        this.ds = new DatagramSocket(port);
    }

    public static void main(String[] args) {
        System.out.println(FLLReceiver.class.getName());
    }

    public String receive() throws IOException {
            byte[] receive = new byte[65535];

            DatagramPacket RPacket = new DatagramPacket(receive, receive.length);

            this.ds.receive(RPacket);

            DatagramPacket sendPacket = new DatagramPacket("ack".getBytes(), "ack".getBytes().length,
                                                            RPacket.getAddress(), RPacket.getPort());
            this.ds.send(sendPacket);
            return Utility.data(receive).toString();
    }
}
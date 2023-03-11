package pt.tecnico.ulisboa;

import java.io.IOException;
import java.net.*;

public class FLL {

    private final DatagramSocket ds;
    public FLL(String hostName, int port) throws SocketException, UnknownHostException {
        this.ds = new DatagramSocket(port);
    }

    public boolean send(byte[] message, String hostName, int port) throws IOException {
        InetAddress address = InetAddress.getByName(hostName);
        DatagramPacket sendPacket = new DatagramPacket(message, message.length, address, port);
        this.ds.send(sendPacket);
        return true;
    }

    public String receive() throws IOException {
        byte[] receive = new byte[65535];

        DatagramPacket RPacket = new DatagramPacket(receive, receive.length);

        this.ds.receive(RPacket);
        String message = Utility.data(receive).toString();
        if(!message.equals("ack")) {
            DatagramPacket sendPacket = new DatagramPacket("ack".getBytes(), "ack".getBytes().length,
                    RPacket.getAddress(), RPacket.getPort());
            //Send ack
            this.ds.send(sendPacket);
        }
        return message;
    }

}

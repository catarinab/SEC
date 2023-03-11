package pt.tecnico.ulisboa;

import java.io.IOException;
import java.net.*;

public class FLL {

    private final DatagramSocket ds;
    private final InetAddress address;
    private final int port;
    public FLL(String hostName, int port, Utility.Type type) throws SocketException, UnknownHostException {
        if(type == Utility.Type.CLIENT) this.ds = new DatagramSocket();
        else this.ds = new DatagramSocket(port);
        this.address = InetAddress.getByName(hostName);
        this.port = port;
    }

    public boolean send(byte[] message) throws IOException {
        DatagramPacket SPacket = new DatagramPacket(message, message.length, this.address, this.port);
        this.ds.send(SPacket);

        byte[] rData = new byte[1024];
        DatagramPacket RPacket = new DatagramPacket(rData, rData.length);
        this.ds.receive(RPacket);
        return Utility.data(rData).toString().equals("ack");
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

        DatagramPacket sendPacket = new DatagramPacket("ack".getBytes(), "ack".getBytes().length,
                RPacket.getAddress(), RPacket.getPort());
        //Send ack
        this.ds.send(sendPacket);
        return Utility.data(receive).toString();
    }

}

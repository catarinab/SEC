package pt.tecnico.ulisboa;
import java.io.IOException;
import java.net.*;

//Server

public class FLLReceiver implements FLL{
    private final DatagramSocket ds;
    public FLLReceiver(String hostName, int port) throws SocketException, UnknownHostException {
        this.ds = new DatagramSocket(port);
    }

    public static void main(String[] args) {
        System.out.println(FLLReceiver.class.getName());
    }

    //cant send messages besides sending to servers
    public boolean send(byte[] message) throws IOException {
        return false;
    }

    //send messages to servers
    @Override
    public boolean send(byte[] message, String hostName, int port) throws IOException {
        InetAddress address = InetAddress.getByName(hostName);
        DatagramPacket sendPacket = new DatagramPacket("ola".getBytes(), "ola".getBytes().length,
                address, port);
        this.ds.send(sendPacket);
        return true;
    }

    @Override
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
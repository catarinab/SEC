package pt.tecnico.ulisboa;
import java.io.IOException;
import java.net.*;

public class FLLSender implements FLL{
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

    @Override
    public boolean send(byte[] message) throws IOException {
        DatagramPacket SPacket = new DatagramPacket(message, message.length, this.address, this.port);

        this.ds.send(SPacket);

        byte[] rData = new byte[1024];
        DatagramPacket RPacket = new DatagramPacket(rData, rData.length);
        this.ds.receive(RPacket);
        return Utility.data(rData).toString().equals("ack");
    }

    @Override
    public boolean send(byte[] message, String hostname, int port) throws IOException {
        return false;
    }

    //Doesnt receive messages besides ack
    @Override
    public String receive() throws IOException {
        return null;
    }
}
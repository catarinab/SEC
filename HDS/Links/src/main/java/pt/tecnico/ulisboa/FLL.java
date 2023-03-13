package pt.tecnico.ulisboa;

import org.json.JSONObject;

import java.io.IOException;
import java.net.*;

import pt.tecnico.ulisboa.Utility;

public class FLL {

    private final DatagramSocket ds;
    public FLL(String hostName, int port) throws SocketException, UnknownHostException {
        this.ds = new DatagramSocket(port);
    }

    public String send(byte[] message, String hostName, int port) throws IOException {
        InetAddress address = InetAddress.getByName(hostName);
        DatagramPacket sendPacket = new DatagramPacket(message, message.length, address, port);
        this.ds.send(sendPacket);
        byte[] receive = new byte[65535];

        DatagramPacket RPacket = new DatagramPacket(receive, receive.length);

        this.ds.receive(RPacket);

        return Utility.data(receive).toString();
    }

    public String receive() throws IOException {
        byte[] receive = new byte[65535];

        DatagramPacket RPacket = new DatagramPacket(receive, receive.length);

        this.ds.receive(RPacket);
        String message = Utility.data(receive).toString();
        if(!message.equals("ack")) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("mac", Utility.getMacFromJson(message));
                jsonObject.put("command", "ack");
                DatagramPacket sendPacket = new DatagramPacket(jsonObject.toString().getBytes(),
                        jsonObject.toString().getBytes().length, RPacket.getAddress(), RPacket.getPort());
                //Send ack
                this.ds.send(sendPacket);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return message;
    }

}

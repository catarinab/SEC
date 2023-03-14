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

        this.ds.setSoTimeout(1000);
        this.ds.receive(RPacket);

        return Utility.data(receive).toString();
    }

    public String receive() throws IOException {
        String message = "";
        try {
            this.ds.setSoTimeout(1000);
            byte[] receive = new byte[65535];

            DatagramPacket RPacket = new DatagramPacket(receive, receive.length);

            this.ds.receive(RPacket);
            message = Utility.data(receive).toString();
            JSONObject jsonObjectReceived = new JSONObject(message);
            if(!jsonObjectReceived.getString("command").equals("ack")) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("mac", Utility.getMacFromJson(message));
                jsonObject.put("command", "ack");
                this.ds.setSoTimeout(0);
                DatagramPacket sendPacket = new DatagramPacket(jsonObject.toString().getBytes(),
                        jsonObject.toString().getBytes().length, RPacket.getAddress(), RPacket.getPort());
                this.ds.send(sendPacket);
            }
        }
        catch(Exception e) {
        }
        return message;
    }

}

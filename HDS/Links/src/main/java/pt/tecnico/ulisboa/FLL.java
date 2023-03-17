package pt.tecnico.ulisboa;

import org.json.JSONObject;

import java.io.IOException;
import java.net.*;


public class FLL {

    private final DatagramSocket ds;
    public FLL(String hostName, int port) throws SocketException, UnknownHostException {
        this.ds = new DatagramSocket(port);
    }

    public void send(byte[] message, String hostName, int port) throws IOException {
        InetAddress address = InetAddress.getByName(hostName);
        DatagramPacket sendPacket = new DatagramPacket(message, message.length, address, port);
        this.ds.send(sendPacket);
    }

    public String receive() throws IOException {
        byte[] receive = new byte[65535];
        DatagramPacket RPacket = new DatagramPacket(receive, receive.length);
        this.ds.receive(RPacket);

        String message = Utility.data(receive).toString();
        JSONObject jsonObjectReceived = new JSONObject(message);
        if(!jsonObjectReceived.getString("command").equals("ack")) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("mac", Utility.getMacFromJson(message));
            jsonObject.put("command", "ack");
            DatagramPacket sendPacket = new DatagramPacket(jsonObject.toString().getBytes(),
                    jsonObject.toString().getBytes().length, RPacket.getAddress(), RPacket.getPort());
            this.ds.send(sendPacket);
        }

        return message;
    }

    public DatagramSocket getDs() {
        return ds;
    }
}

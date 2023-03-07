package java.pt.tecnico.ulisboa;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

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

    public void receive() throws IOException {
        byte buf[] = null;
        Scanner sc = new Scanner(System.in);
        while (true) {
            String inp = sc.nextLine();
            buf = inp.getBytes();

            DatagramPacket DpSend = new DatagramPacket(buf, buf.length, this.address, this.port);

            ds.send(DpSend);

            // break the loop if user enters "bye"
            if (inp.equals("bye")) break;
        }
    }

    public void send() {

    }
}
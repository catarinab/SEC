package pt.tecnico.ulisboa;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
public class Service {

    private final APL apl;
    public Service() throws SocketException, UnknownHostException {
        this.apl = new APL("localhost", 1234, Utility.Type.SERVER);
    }

    public static void main(String[] args) throws IOException {
        System.out.println(Service.class.getName());
        Service service = new Service();
        while(true) service.receive();
    }

    public void receive() throws IOException {
        String message = this.apl.receive();
        if(message == null) return;
    }
}
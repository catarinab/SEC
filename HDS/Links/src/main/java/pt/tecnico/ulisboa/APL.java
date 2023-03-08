package pt.tecnico.ulisboa;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.json.CDL;
import org.json.JSONArray;
import pt.tecnico.ulisboa.Utility.Type;

//por enquanto fica apenas PL
public class APL {
    private final StubbornLink stubbornLink;
    private ArrayList<String> delivered = new ArrayList<String>();

    private int messageCounter = 0;

    public APL(String hostname, int port, Type type) throws SocketException, UnknownHostException {
        this.stubbornLink = new StubbornLink(hostname, port, 10, 2, type);
    }

    public boolean send(String message) throws IOException, InterruptedException {
        this.messageCounter++;
        JSONArray jsonArray = new JSONArray();
        jsonArray.put("messageID");
        jsonArray.put("append");
        String csvData = this.messageCounter+","+message;

        return this.stubbornLink.send(String.valueOf(CDL.toJSONArray(jsonArray,csvData)));
    }

    public String receive() throws IOException {
        String message = this.stubbornLink.receive();
        if(!delivered.contains(message)) {
            delivered.add(message);
            System.out.println("mensagem repetida");
            return message;
        }
        return null;
    }
}

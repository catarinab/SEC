package pt.tecnico.ulisboa;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map.Entry;
import java.util.List;

public class Broadcast {
    private final List<Entry<String,Integer>> processes = new java.util.ArrayList<>();
    private final APL apl;

    public Broadcast(List<Entry<String,Integer>> processes, APL apl) {
        this.processes.addAll(processes);
        this.apl = apl;
    }

    public void doBroadcast(String inputValue, String message) throws IOException, InterruptedException, NoSuchPaddingException,
            IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        for(Entry<String, Integer> process: processes) {
            System.out.println("Sending message: "+message);
            System.out.println("sending to: "+process.getKey()+ process.getValue());
            apl.send(inputValue, message, process.getKey(), process.getValue());
        }
    }
}
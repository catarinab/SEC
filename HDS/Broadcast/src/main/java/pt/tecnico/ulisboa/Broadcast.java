package pt.tecnico.ulisboa;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.List;

public class Broadcast {
    private final List<Entry<String,Integer>> processes= new java.util.ArrayList<>();
    private final Entry<String, Integer> host;
    private final APL apl;

    public Broadcast(Entry<String, Integer> host, List<Entry<String,Integer>> processes, APL apl) {
        this.processes.addAll(processes);
        this.host = host;
        this.apl = apl;
    }

    public void doBroadcast(String message) throws IOException, InterruptedException {
        for(Entry<String, Integer> process: processes) {
            apl.send(message, process.getKey(), process.getValue());
        }
    }
}
package pt.tecnico.ulisboa;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.List;

public class Broadcast {
    private final List<Entry<String,Integer>> processes= new java.util.ArrayList<>();

    private final APLServer aplServer;

    public Broadcast(Entry<String, Integer> host, List<Entry<String,Integer>> processes, APLServer aplServer) {
        this.processes.addAll(processes);
        this.aplServer = aplServer;
    }

    public void doBroadcast() throws IOException, InterruptedException {
        for(Entry<String, Integer> process: processes) {
            aplServer.send("ola", process.getKey(), process.getValue());
        }
    }
}
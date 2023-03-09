package pt.tecnico.ulisboa;

import java.util.Map.Entry;
import java.util.List;

public class Broadcast {
    private final List<Entry<String,Integer>> processes= new java.util.ArrayList<>();
    private final Entry<String,Integer> host;

    public Broadcast(Entry<String, Integer> host, List<Entry<String,Integer>> processes) {
        this.host = host;
        this.processes.addAll(processes);
    }

}
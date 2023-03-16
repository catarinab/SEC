package pt.tecnico.ulisboa;

import java.io.IOException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Map.Entry;
import java.net.ServerSocket;

//Used strictly for testing.
public class ServiceAux extends Thread{
    private final Service server;

    public ServiceAux(String hostname, int port, boolean byzantine, int byzantineProcesses, List<Entry<String,Integer>> processes, boolean leader) throws IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        this.server = new Service(hostname, port, byzantine, byzantineProcesses, processes, leader);
    }

    public boolean isInBlockchain(String data) {
        return this.server.isInBlockchain(data);
    }

    public String getBlockchainIndex(int index) {
        return this.server.getBlockchainIndex(index);
    }

    public List<String> getBlockchainData() {
        return this.server.getBlockchainData();
    }

    public void run() {
        try {
            this.server.receive();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
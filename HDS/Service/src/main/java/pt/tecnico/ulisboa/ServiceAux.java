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
    private boolean run = true;

    public ServiceAux(String hostname, int port, boolean byzantine, int byzantineProcesses, List<Entry<String,Integer>> processes, boolean leader, Entry<String,Integer> leaderID) throws IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        this.server = new Service(hostname, port, byzantine, byzantineProcesses, processes, leader, leaderID);
        this.run = true;
    }

    public Service getServer(){
        return this.server;
    }

    public boolean isInBlockchain(String data) {
        return this.server.isInBlockchain(data);
    }

    public String getBlockchainPrint() {
        return this.server.getBlockchainPrint();
    }

    public boolean predictedBlockchain(String predictedBlockchain) {return this.getBlockchainPrint().equals(predictedBlockchain);}

    public String getBlockchainIndex(int index) {
        return this.server.getBlockchainIndex(index);
    }

    public List<String> getBlockchainData() {
        return this.server.getBlockchainData();
    }

    public String getPublicKey(int account) { return this.server.getPublicKey(account); }
    public void run() {
        try {
            while(true){
                this.server.receive();
            }

        }
        catch (Exception e) {
        }
    }
}
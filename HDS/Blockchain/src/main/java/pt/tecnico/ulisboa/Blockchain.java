package pt.tecnico.ulisboa;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;

public class Blockchain {
    private final LinkedList<Block> chain;
    private int maxTransactions;

    public Blockchain(int maxTransactions) {
        this.chain = new LinkedList<>();
        this.maxTransactions = maxTransactions;
    }

    public void addValue(Block block) { this.chain.add(block);
    }

    public synchronized String getBlockchainIndex(int index) {
        return this.chain.get(index).getData();
    }

    public synchronized int getMaxTransactions(){
        return this.maxTransactions;
    }

    public int check_balance(String publicKey){
        int balance = -1;
        Iterator<Block> listIterator = this.chain.descendingIterator();
        while (listIterator.hasNext()) {
            balance = listIterator.next().check_balance(publicKey);
            if (balance != -1) break;
        }
        return balance;
    }

    public synchronized ArrayList<String> getBlockchainData() {
        ArrayList<String> data = new ArrayList<>();
        for (Block block: this.chain) data.add(block.getData());
        return data;
    }

    public synchronized void printBlockchain() {
        int i = 0;
        System.out.println("Blockchain: ");
        for (Block block: this.chain) {
            System.out.println("Block " + i++ + ":" + block.getData());
        }
    }
}
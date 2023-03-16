package pt.tecnico.ulisboa;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Blockchain {
    private final LinkedList<Block> chain;

    public Blockchain() {
        this.chain = new LinkedList<Block>();
    }

    public synchronized void addValue(String inputValue) {
        String lastHash = ((this.chain.size() == 0) ? "" : this.chain.getLast().getHash());
        this.chain.add(new Block(lastHash, inputValue));
    }

    public synchronized String getBlockchainIndex(int index) {
        return this.chain.get(index).getData();
    }

    public synchronized ArrayList<String> getBlockchainData() {
        ArrayList<String> data = new ArrayList<>();
        for (Block block: this.chain) data.add(block.getData());
        return data;
    }

    public synchronized void printBlockchain() {
        int i = 0;
        int size = this.chain.size();
        System.out.print("Blockchain: ");
        for (Block block: this.chain) {
            if (i == size - 1) System.out.println(i++ + "- " + block.getData());
            else System.out.print(i++ + "- " + block.getData() + ", ");
        }
    }
    
}
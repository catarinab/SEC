package pt.tecnico.ulisboa;
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

    public synchronized List<Block> getBlockchain() {
        return this.chain;
    }

    public synchronized void printBlockchain() {
        for (Block block : this.chain){
            System.out.println(block.getHash() + " , "+block.getData());
        }
    }
    
}
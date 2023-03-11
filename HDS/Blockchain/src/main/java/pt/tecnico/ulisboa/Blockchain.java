package pt.tecnico.ulisboa;
import java.util.ArrayList;
import java.util.List;

public class Blockchain {
    private final ArrayList<Block> chain;

    public Blockchain() {
        this.chain = new ArrayList<Block>();
    }

    public void addBlock(Block block) {
        this.chain.add(block);
    }

    public List<Block> getBlockchain() {
        return this.chain;
    }
    
}
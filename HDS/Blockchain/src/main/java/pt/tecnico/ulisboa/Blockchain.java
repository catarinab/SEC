package pt.tecnico.ulisboa;
import java.util.ArrayList;
public class Blockchain {
    public static ArrayList<Block> blockchain = new ArrayList<Block>();

    public Blockchain() {
        chain = new ArrayList<Block>();
    }

    public void addBlock(Block block) {
        chain.add(block);
    }

    public List<Block> getBlockchain() {
        return chain;
    }
    
}
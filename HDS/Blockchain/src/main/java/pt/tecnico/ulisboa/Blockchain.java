package pt.tecnico.ulisboa;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Blockchain {
    private final LinkedList<Block> chain;
    private Block currBlock;
    private int maxTransactions;

    public Blockchain(int maxTransactions) {
        this.chain = new LinkedList<Block>();
        this.currBlock = new Block("", maxTransactions);
        this.maxTransactions = maxTransactions;
    }

    public void addValue(String transaction) {
        OperationDTO op = new OperationDTO(transaction);
        if(!this.currBlock.addTransaction(op)) {
            this.chain.add(this.currBlock);
            String previousHash = this.currBlock.getHash();
            this.currBlock = new Block(previousHash, this.maxTransactions);
            this.currBlock.addTransaction(op);
        }

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
            if (i == size - 1) System.out.println("Block : " + i++ + "- \n" + block.getData());
            else System.out.print("Block : " + i++ + "- " + block.getData() + ", ");
        }
    }
    
}
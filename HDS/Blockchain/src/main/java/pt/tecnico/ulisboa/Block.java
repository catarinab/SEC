package pt.tecnico.ulisboa;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Block {
    //hash of the data
    private String hash;
    //hash from previous block, to make sure the blockchain is not tampered with
    private final String previousHash;
    private final OperationDTO[] transactionGroup;
    private int transactions = 0;
    private final int maxTransactions;

    public Block(String previousHash, int maxTransactions) {
        this.previousHash = previousHash;
        this.transactionGroup = new OperationDTO[maxTransactions];
        this.maxTransactions = maxTransactions;
    }

    public void byzantine(){
        for(int i = 0; i < this.transactions; i++){
            this.transactionGroup[i].multiplyCurrBalance();
        }
    }

    public synchronized boolean addTransaction(OperationDTO transaction) {
        System.out.println(this.maxTransactions);
        if (this.transactions >= this.maxTransactions) return false;
        else transactionGroup[this.transactions++] = transaction;
        System.out.println("adicionou no bloco no indice" + (this.transactions - 1));
        return true;
    }

    public OperationDTO[] getTransactionGroup() {
        return transactionGroup;
    }

    public void calculateHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String stringToHash = previousHash + Arrays.toString(transactionGroup);
            this.hash = Arrays.toString(digest.digest(stringToHash.getBytes(StandardCharsets.UTF_8)));
        }
        catch(NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }

    public String getHash(){
        calculateHash();
        return this.hash;
    }

    public String getData(){
        StringBuilder retVal = new StringBuilder();
        for(int i = 0; i < this.transactions; i++) {
            retVal.append(i).append(" -> source: ").append(this.transactionGroup[i]);
        }
        return retVal.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) return false;

        final Block block = (Block) obj;
        if(this.transactions != block.transactions) return false;
        OperationDTO[] blockOps = block.getTransactionGroup();
        for(int i = 0; i < this.transactions; i++){
            if(!this.transactionGroup[i].equals(blockOps[i])) return false;
        }

        return true;
    }
    
}
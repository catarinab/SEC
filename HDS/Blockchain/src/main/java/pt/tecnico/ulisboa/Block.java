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
    private int currTransactionNum = 0;
    private final int maxTransactions;

    public Block(String previousHash, int maxTransactions) {
        this.previousHash = previousHash;
        this.transactionGroup = new OperationDTO[maxTransactions];
        this.maxTransactions = maxTransactions;
    }

    public synchronized boolean addTransaction(OperationDTO transaction) {
        if(this.currTransactionNum >= this.maxTransactions) return false;
        else transactionGroup[currTransactionNum++] = transaction;
        System.out.println("adicionou no bloco no indice" + (currTransactionNum-1));
        return true;
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
        for(int i = 0; i < this.currTransactionNum; i++) {
            retVal.append(i).append(" -> source: ").append(this.transactionGroup[i].account).append(", previous balance: ")
                    .append(this.transactionGroup[i].previousBalance).append(", new balance: ")
                    .append(this.transactionGroup[i].currBalance).append("\n");
        }
        return retVal.toString();
    }
    
}
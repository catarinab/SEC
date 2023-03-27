package pt.tecnico.ulisboa;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Block {
    //hash of the data
    private String hash;
    //hash from previous block, to make sure the blockchain is not tampered with
    private final String previousHash;
    private final TransactionDTO[] transactionGroup;
    private int currTransactionNum = 0;
    private final int maxTransactions;

    public Block(String previousHash, int maxTransactions) {
        this.previousHash = previousHash;
        this.transactionGroup = new TransactionDTO[maxTransactions];
        this.maxTransactions = maxTransactions;
    }

    public synchronized boolean addTransaction(TransactionDTO transaction) {
        if(this.currTransactionNum >= this.maxTransactions) return false;
        else transactionGroup[currTransactionNum++] = transaction;
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
    
}
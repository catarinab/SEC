package pt.tecnico.ulisboa;

import org.json.JSONObject;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;

public class Block {
    //hash of the data
    private String hash;
    //hash from previous block, to make sure the blockchain is not tampered with
    private String previousHash;
    private OperationDTO[] transactionGroup;
    private int transactions = 0;
    private final int maxTransactions;

    public Block(String previousHash, int maxTransactions) {
        this.previousHash = previousHash;
        this.transactionGroup = new OperationDTO[maxTransactions];
        this.maxTransactions = maxTransactions;
    }

    public Block(JSONObject jsonObject) {
        this.previousHash = jsonObject.getString("previousHash");
        this.transactions = jsonObject.getInt("transactions");
        this.maxTransactions = jsonObject.getInt("maxTransactions");
        this.transactionGroup = new OperationDTO[maxTransactions];
        for (int i = 0; i < this.maxTransactions; i++) {
            JSONObject transaction = jsonObject.getJSONObject(Integer.toString(i));
            String type = transaction.getString("transaction");
            if(type.equals("createAcc")) this.transactionGroup[i] = new CreateAccDTO(transaction);
            else if(type.equals("transfer")) this.transactionGroup[i] = new TransferDTO(transaction);
            else throw new RuntimeException();
        }
    }

    public synchronized JSONObject toJsonObj() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("previousHash", this.previousHash);
        jsonObject.put("transactions", this.transactions);
        jsonObject.put("maxTransactions", this.maxTransactions);
        for (int i = 0; i < this.maxTransactions; i++) {
            jsonObject.put(Integer.toString(i), this.transactionGroup[i].toJsonObj());
        }
        return jsonObject;
    }

    public synchronized void byzantine(){
        for(int i = 0; i < this.transactions; i++){
            this.transactionGroup[i].multiplyCurrBalance(1000);
        }
    }

    public synchronized boolean addTransaction(OperationDTO transaction) {
        transactionGroup[this.transactions++] = transaction;
        System.out.println("Appended to block in index: " + (this.transactions - 1));
        return (this.transactions < this.maxTransactions);
    }

    public synchronized OperationDTO[] getTransactionGroup() {
        return transactionGroup;
    }

    public synchronized void calculateHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String stringToHash = previousHash + Arrays.toString(transactionGroup);
            this.hash = Arrays.toString(digest.digest(stringToHash.getBytes(StandardCharsets.UTF_8)));
        }
        catch(NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }

    public synchronized int check_balance(String publicKey){
        int balance = -1;
        for (int i = transactionGroup.length - 1; i >= 0 && balance == -1; i--) {
            balance = transactionGroup[i].check_balance(publicKey);
        }
        return balance;
    }

    public synchronized String getData(){
        StringBuilder retVal = new StringBuilder();
        for(int i = 0; i < this.transactions; i++) {
            if (i > 0) retVal.append("\t");
            retVal.append(i).append(" -> source: ").append(this.transactionGroup[i]);
            if (i < this.transactions - 1) retVal.append("\n");
        }
        return retVal.toString();
    }

    public synchronized HashMap<String, Integer> getAccountsBalance(){
        HashMap<String, Integer> accountsBalance = new HashMap<>();
        for(int i = this.maxTransactions - 1; i >= 0 ; i--) {
            if(!accountsBalance.containsKey(transactionGroup[i].publicKey)) accountsBalance.put(transactionGroup[i].publicKey, transactionGroup[i].currBalance);
            if(TransferDTO.class.isAssignableFrom(transactionGroup[i].getClass())) {
                TransferDTO transfer = (TransferDTO) transactionGroup[i];
                if(!accountsBalance.containsKey(transfer.destination)) accountsBalance.put(transfer.destination, transfer.currBalanceDest);
            }
        }
        return accountsBalance;
    }

    public synchronized void reset() {
        calculateHash();
        this.previousHash = this.hash;
        this.hash = "";
        this.transactionGroup = new OperationDTO[maxTransactions];
        this.transactions = 0;
    }

    @Override
    public synchronized boolean equals(Object obj) {
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
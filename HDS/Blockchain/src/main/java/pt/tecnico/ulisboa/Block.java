package pt.tecnico.ulisboa;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Block {
    //hash of the data
    private final String hash;
    //hash from previous block, to make sure the blockchain is not tampered with
    private final String previousHash;
    private final String data;

    public Block(String previousHash, String data) {
        this.previousHash = previousHash;
        this.hash = calculateHash(this.previousHash, data);
        this.data = data;
    }

    public String calculateHash(String previousHash, String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String stringToHash = previousHash + data;
            return Arrays.toString(digest.digest(stringToHash.getBytes(StandardCharsets.UTF_8)));
        }
        catch(NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }

    public String getData(){
        return this.data;
    }

    public String getHash(){
        return this.hash;
    }
    
}
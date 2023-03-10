package pt.tecnico.ulisboa;

public class Block {
    //hash of the data
    private String hash;
    //hash from previous block, to make sure the blockchain is not tampered with
    private String previousHash;
    private String data;

    public Block(String hash, String previousHash, String data) {
        this.previousHash = previousHash;
        this.hash = calculateHash(this.previousHash);
        this.data = data;
    }

    public String calculateHash(String previousHash) {
        return "ola";
        //return crypt.sha256(previousHash + data);
    }
    
}
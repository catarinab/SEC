package pt.tecnico.ulisboa;

import org.json.JSONObject;
import java.util.Random;

//Data Transfer Object for Operations (transfers and create account)
public abstract class OperationDTO {
    public String digSignature;
    public String publicKey;
    public int currBalance;

    public OperationDTO(String publicKey, String digSignature, int currBalance) {
        this.publicKey = publicKey;
        this.digSignature = digSignature;
        this.currBalance = currBalance;
    }

    public abstract JSONObject toJsonObj();

    //for byzantine purposes
    public void multiplyCurrBalance() {
        Random rand = new Random();
        this.currBalance *= (rand.nextInt(50) + 1) ;
    }
}

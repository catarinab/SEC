package pt.tecnico.ulisboa;

import java.security.Key;
import java.security.PublicKey;
import java.util.Random;

//Data Transfer Object for Operations (transfers and create account)
public class OperationDTO {
    public String publicKey;
    public int currBalance = 0;
    public int prevBalance = 0;
    public String digSignature;

    public OperationDTO(String publicKey, int currBalance, int prevBalance, String digSignature) {
        this.publicKey = publicKey;
        this.currBalance = currBalance;
        this.prevBalance = prevBalance;
        this.digSignature = digSignature;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) return false;

        final OperationDTO op = (OperationDTO) obj;
        if (!this.publicKey.equals(op.publicKey) || this.currBalance != op.currBalance ||
                this.prevBalance != op.prevBalance || this.digSignature != op.digSignature) return false;

        return true;
    }

    //for byzantine porpuses
    public void multiplyCurrBalance(){
        Random rand = new Random();
        this.currBalance *= (rand.nextInt(50) + 1) ;
    }
}

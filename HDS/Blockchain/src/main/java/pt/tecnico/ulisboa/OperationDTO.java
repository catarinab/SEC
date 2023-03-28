package pt.tecnico.ulisboa;

import java.security.Key;
import java.security.PublicKey;

//Data Transfer Object for Operations (transfers and create account)
public class OperationDTO {
    public String account = "";
    public int currBalance = 0;
    public int previousBalance = 0;
    public String digSignature;
    public OperationDTO(String account) {
        this.account = account;
    }
}

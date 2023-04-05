package pt.tecnico.ulisboa;

import org.json.JSONObject;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Random;

//Data Transfer Object for Operations (transfers and create account)
public abstract class OperationDTO {
    public String digSignature;
    public String publicKey;
    public int currBalance;
    public final String hostname;
    public final int port;

    public OperationDTO(String publicKey, String digSignature, int currBalance, String hostname, int port) {
        this.publicKey = publicKey;
        this.digSignature = digSignature;
        this.currBalance = currBalance;
        this.hostname = hostname;
        this.port = port;
    }

    public abstract JSONObject toJsonObj();

    //for byzantine purposes
    public void multiplyCurrBalance(int x) {
        this.currBalance *= x;
    }

    public String getDigSignature(){
        return this.digSignature;
    }

    public Map.Entry<String, Integer> getClientID() {
        return new AbstractMap.SimpleEntry<>(this.hostname, this.port);
    }
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) return false;

        final OperationDTO op = (OperationDTO) obj;

        return this.digSignature.equals(op.digSignature) && this.publicKey.equals(op.publicKey) && this.port == op.port
                && this.hostname.equals(op.hostname) && this.currBalance == op.currBalance;
    }
}

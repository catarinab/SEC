package pt.tecnico.ulisboa;

import org.json.JSONObject;

public class TransferDTO extends OperationDTO{
    public String destination;
    public int prevBalanceSource;
    public int prevBalanceDest;
    public int currBalanceDest;
    public int amount;

    public TransferDTO(String publicKey, String digSignature, int prevBalanceSource, int currBalanceSource,
                       int prevBalanceDest, int currBalanceDest, String destination, int amount, String hostname,
                       int port) {
        super(publicKey, digSignature, currBalanceSource, hostname, port);
        this.prevBalanceSource = prevBalanceSource;
        this.prevBalanceDest = prevBalanceDest;
        this.currBalanceDest = currBalanceDest;
        this.destination = destination;
        this.amount = amount;
    }

    public TransferDTO(JSONObject jsonObject){
        super(jsonObject.getString("publicKey"), jsonObject.getString("digSignature"),
                jsonObject.getInt("currBalanceSource"), jsonObject.getString("hostname"),
                jsonObject.getInt("port"));
        this.prevBalanceSource = jsonObject.getInt("prevBalanceSource");
        this.prevBalanceDest = jsonObject.getInt("prevBalanceDest");
        this.currBalanceDest = jsonObject.getInt("currBalanceDest");
        this.destination = jsonObject.getString("destination");
        this.amount = jsonObject.getInt("amount");

    }

    public JSONObject toJsonObj (){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("transaction", "transfer");
        jsonObject.put("publicKey", super.publicKey);
        jsonObject.put("digSignature", super.digSignature);
        jsonObject.put("hostname", super.hostname);
        jsonObject.put("port", super.port);
        jsonObject.put("prevBalanceSource", this.prevBalanceSource);
        jsonObject.put("currBalanceSource", super.currBalance);
        jsonObject.put("prevBalanceDest", this.prevBalanceDest);
        jsonObject.put("currBalanceDest", this.currBalanceDest);
        jsonObject.put("destination", this.destination);
        jsonObject.put("amount", this.amount);
        return jsonObject;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) return false;

        final TransferDTO op = (TransferDTO) obj;
        return super.equals(obj) && this.prevBalanceSource == op.prevBalanceSource
                && this.prevBalanceDest == op.prevBalanceDest && this.currBalanceDest == op.currBalanceDest
                && this.destination.equals(op.destination) && this.amount == op.amount;
    }

    @Override
    public String toString(){
        return "Transfer from account: " + this.publicKey + " with previous balance"+ this.prevBalanceSource+
                ", to destination account" + this.destination + ", with the value: " + this.amount;
    }
}

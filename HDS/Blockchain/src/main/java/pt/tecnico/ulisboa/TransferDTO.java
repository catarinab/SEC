package pt.tecnico.ulisboa;

import org.json.JSONObject;

public class TransferDTO extends OperationDTO{
    public int prevBalance;
    public String destination;
    public int amount;

    public TransferDTO(String publicKey, String digSignature, int currBalance, int prevBalance, String destination,
                       int amount) {
        super(publicKey, digSignature, currBalance);
        this.prevBalance = prevBalance;
        this.destination = destination;
        this.amount = amount;
    }

    public TransferDTO(JSONObject jsonObject){
        super(jsonObject.getString("publicKey"), jsonObject.getString("digSignature"),
                jsonObject.getInt("currBalance"));
        this.prevBalance = jsonObject.getInt("prevBalance");
        this.destination = jsonObject.getString("destination");
        this.amount = jsonObject.getInt("amount");

    }

    public JSONObject toJsonObj (){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("transaction", "transfer");
        jsonObject.put("publicKey", super.publicKey);
        jsonObject.put("digSignature", super.digSignature);
        jsonObject.put("currBalance", this.currBalance);
        jsonObject.put("prevBalance", prevBalance);
        jsonObject.put("destination", this.destination);
        jsonObject.put("amount", amount);
        return jsonObject;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) return false;

        final TransferDTO op = (TransferDTO) obj;
        return this.publicKey.equals(op.publicKey) && this.currBalance == op.currBalance &&
                this.digSignature.equals(op.digSignature);
    }

    @Override
    public String toString(){
        return "Transfer from account: " + this.publicKey + " with previous balance"+ this.prevBalance+
                ", to destination account" + this.destination + ", with the value: " + this.amount +"\n";
    }
}

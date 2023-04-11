package pt.tecnico.ulisboa;

import org.json.JSONObject;

public class BalanceDTO extends OperationDTO{

    public BalanceDTO(String publicKey, int currBalance) {
        super(publicKey, currBalance);
    }

    public BalanceDTO(JSONObject jsonObject){
        super(jsonObject.getString("publicKey"), jsonObject.getInt("currBalance"));
    }

    public JSONObject toJsonObj (){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("publicKey", this.publicKey);
        jsonObject.put("currBalance", this.currBalance);
        return jsonObject;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) return false;

        final BalanceDTO op = (BalanceDTO) obj;
        return this.publicKey.equals(op.publicKey) && this.currBalance == op.currBalance;
    }

    @Override
    public String toString(){
        return "Create Account: " + this.publicKey + ", new balance: " + this.currBalance;
    }

}

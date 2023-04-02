package pt.tecnico.ulisboa;

import org.json.JSONObject;

public class CreateAccDTO extends OperationDTO{

    public CreateAccDTO(String publicKey, String digSignature, int currBalance) {
        super(publicKey, digSignature, currBalance);
    }

    public CreateAccDTO(JSONObject jsonObject){
        super(jsonObject.getString("publicKey"), jsonObject.getString("digSignature"),
                jsonObject.getInt("currBalance"));
    }

    public JSONObject toJsonObj (){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("transaction", "createAcc");
        jsonObject.put("publicKey", this.publicKey);
        jsonObject.put("digSignature", this.digSignature);
        jsonObject.put("currBalance", this.currBalance);
        return jsonObject;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) return false;

        final CreateAccDTO op = (CreateAccDTO) obj;
        return this.publicKey.equals(op.publicKey) && this.currBalance == op.currBalance &&
        this.digSignature.equals(op.digSignature);
    }
    @Override
    public String toString(){
        return "Create Account: " + this.publicKey + ", new balance: " + this.currBalance + "\n";
    }
}

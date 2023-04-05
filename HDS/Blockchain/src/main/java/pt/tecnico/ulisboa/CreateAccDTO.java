package pt.tecnico.ulisboa;

import org.json.JSONObject;

public class CreateAccDTO extends OperationDTO{

    public CreateAccDTO(String publicKey, String digSignature, int currBalance, String hostname, int port) {
        super(publicKey, digSignature, currBalance, hostname, port);
    }

    public CreateAccDTO(JSONObject jsonObject){
        super(jsonObject.getString("publicKey"), jsonObject.getString("digSignature"),
                jsonObject.getInt("currBalance"), jsonObject.getString("hostname"),
                jsonObject.getInt("port"));
    }

    public JSONObject toJsonObj (){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("transaction", "createAcc");
        jsonObject.put("publicKey", this.publicKey);
        jsonObject.put("digSignature", this.digSignature);
        jsonObject.put("currBalance", this.currBalance);
        jsonObject.put("hostname", this.hostname);
        jsonObject.put("port", this.port);
        return jsonObject;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString(){
        return "Create Account: " + this.publicKey + ", new balance: " + this.currBalance;
    }

}

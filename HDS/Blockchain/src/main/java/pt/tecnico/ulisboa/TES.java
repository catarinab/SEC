package pt.tecnico.ulisboa;

import java.security.PublicKey;
import java.util.concurrent.ConcurrentHashMap;

public class TES {
    private ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<>();
    public void create_account(PublicKey publicKey) {
        this.accounts.put(publicKey.toString(), new Account(publicKey));
    }
    public int check_balance(PublicKey key) {
        return this.accounts.get(key.toString()).check_balance();
    }

    //Como verificar q é mesmo a pessoa? aqui, ou no service?
    public boolean transfer(PublicKey source, PublicKey destination, int amount) {
        Account sourceAcc = this.accounts.get(source.toString());
        Account destinationAcc = this.accounts.get(destination.toString());
        if(amount > 0) {
            if(sourceAcc.check_balance() - amount < 0) return false;
            sourceAcc.removeBalance(amount);
            destinationAcc.addBalance(amount);
        }
        else if(amount < 0) {
            if(destinationAcc.check_balance() - amount < 0) return false;
            destinationAcc.removeBalance(amount);
            sourceAcc.addBalance(amount);
        }
        return true;
    }
}
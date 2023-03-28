package pt.tecnico.ulisboa;

import java.security.*;

public class Account {
    private int balance;
    private final PublicKey publicKey;
    public Account(PublicKey publicKey) {
        this.publicKey = publicKey;
        this.balance = 20;
    }
    public int check_balance(){
        return this.balance;
    }
    public void removeBalance(double removeBalance){
        this.balance -= removeBalance;
    }

    public void addBalance(double addedBalance){
        this.balance += addedBalance;
    }
    public String getPublicKey(){
        return this.publicKey.toString();
    }
}

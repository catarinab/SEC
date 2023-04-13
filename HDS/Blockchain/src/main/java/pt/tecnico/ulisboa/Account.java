package pt.tecnico.ulisboa;

import java.security.*;

public class Account {
    private int balance;
    private final PublicKey publicKey;

    public Account(PublicKey publicKey) {
        this.publicKey = publicKey;
        this.balance = 20;
    }

    public synchronized int check_balance(){
        return this.balance;
    }

    public synchronized void removeBalance(int removeBalance){
        this.balance -= removeBalance;
    }

    public synchronized void addBalance(int addedBalance){
        this.balance += addedBalance;
    }

    public synchronized String getPublicKey(){
        return this.publicKey.toString();
    }

}

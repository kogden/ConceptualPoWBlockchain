package com.company;
import com.transactions.Transaction;

import java.util.ArrayList;
import java.util.Date;

public class Block {

    public String hash;
    public String previousHash;
    public String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
    private long timeStamp;         // number of ms since 1/1/1970
    private int nonce;

    public Block(String previousHash) {    // hash of previous block becomes input for next hash
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }

    public String calculateHash() {
        String calculatedHash = StringUtil.applySha256(
                previousHash +
                        Long.toString(timeStamp) +
                        Integer.toString(nonce) +
                        merkleRoot
        );
        return calculatedHash;
    }

    public void mineBlock(int difficulty) {
        merkleRoot = StringUtil.getMerkleRoot(transactions);

        String target = new String(new char[difficulty]).replace('\0', '0'); //Create a string with difficulty * "0"

        while(!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block Mined! : " + hash);
    }

    //Add transactions to this block
    public boolean addTransaction(Transaction transaction) {
        // process transaction and check if valid, unless block is genesis block then ignore
        if(transaction == null) return false;
        if(!previousHash.equals("0")) {
            if(!transaction.processTransaction()) {
                System.out.println("Transaction failed to process. Discarded");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("Transaction Successfully added to block");
        return true;
    }
}

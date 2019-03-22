package com.transactions;

public class TransactionInput {
    public String transactionOutputId; //reference to transaction outputs -> transaction inputs
    public TransactionOutput UTXO; //contains unspent transaction output

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }
}

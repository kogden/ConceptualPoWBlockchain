package com.transactions;

import com.company.KogsChain;
import com.company.StringUtil;

import java.security.*;
import java.util.ArrayList;

public class Transaction {

    public String transactionId;    // allows hash of transaction
    public PublicKey sender;        // senders address/public key
    public PublicKey recipient;     // recipients address/public key
    public float value;
    public byte[] signature;        // prevents anyone else from spending funds in our wallet

    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

    private static int sequence = 0; // a rough count of how many transactions have been generated

    public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs) {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
    }

    // calculate the transaction hash (gets the id)
    private String calculateHash() {
        sequence++;         // increase sequence to avoid 2 indentical transactions from having the same hash
        return StringUtil.applySha256(
                StringUtil.getStringFromKey(sender) +
                        StringUtil.getStringFromKey(recipient) +
                        Float.toString(value) + sequence
        );
    }

    //Signs all the data we dont want to be tampered with
    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient);
        signature = StringUtil.applyECDSASig(privateKey, data);
    }

    //Verifies the data we signed hasn't been tampered with
    public boolean verifySignature() {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient);
        return StringUtil.verifyECDSASig(sender, data, signature);
    }

    //Returns true if new transaction can be created
    public boolean processTransaction() {
        //verify signature
        if(!verifySignature()) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }

        //gather transaction inputs (Make sure they are unspent)
        for(TransactionInput i : inputs) {
            i.UTXO = KogsChain.UTXOs.get(i.transactionOutputId);
        }

        //check if transaction is valid:
        if(getInputsValue() < KogsChain.minimumTransaction) {
            System.out.println("#Transaction Inputs too small: " + getInputsValue());
            return false;
        }

        //generate transaction outputs:
        float leftOver = getInputsValue() - value; //get value of inputs then the left over change:
        transactionId = calculateHash();
        outputs.add(new TransactionOutput(this.recipient, value, transactionId));
        outputs.add(new TransactionOutput(this.sender, leftOver, transactionId));

        //add outputs to unspent list
        for(TransactionOutput o : outputs) {
            KogsChain.UTXOs.put(o.id, o);
        }

        //remove transaction inputs from UTXO lists as spent:
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue;    //if Transaction can't be found skip
            KogsChain.UTXOs.remove(i.UTXO.id);
        }

        return true;
    }

    // returns sum of inputs(UTXOs) values
    public float getInputsValue() {
        float total = 0;
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue;
            total += i.UTXO.value;
        }
        return total;
    }

    //returns sum of outputs:
    public float getOutputsValue() {
        float total = 0;
        for(TransactionOutput o : outputs) {
            total += o.value;
        }
        return total;
    }
}

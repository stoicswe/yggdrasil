package org.yggdrasil.core.ledger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.yggdrasil.core.ledger.transaction.Transaction;
import org.yggdrasil.core.serialization.HashSerializer;
import org.yggdrasil.core.utils.CryptoHasher;
import org.yggdrasil.core.utils.CryptoKeyGenerator;
import org.yggdrasil.core.utils.DateTimeUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.security.*;
import java.time.ZonedDateTime;

/**
 * The Wallet will contain the ability to send, receive, and store crypto
 * in an encrypted environment. The Wallet will also be capable of creating
 * an identifiable address in order to send and receive value.
 *
 * @since 0.0.5
 * @author nathanielbunch
 */
@JsonInclude
public class Wallet implements Serializable {

    private final PublicKey publicKey;
    private final PrivateKey privateKey;
    private final ZonedDateTime creationDate;
    @JsonSerialize(using = HashSerializer.class)
    private final byte[] address;
    private final BigDecimal balance;
    @JsonSerialize(using = HashSerializer.class)
    private final byte[] walletHash;
    private Signature signature;

    private Wallet(Builder builder) throws NoSuchAlgorithmException {
        this.publicKey = builder.publicKey;
        this.privateKey = builder.privateKey;
        this.creationDate = builder.creationDate;
        this.address = builder.address;
        this.balance = BigDecimal.ZERO;
        this.signature = Signature.getInstance(CryptoKeyGenerator.getSignatureAlgorithm());
        this.walletHash = CryptoHasher.hash(this);
    }

    private Wallet(PublicKey publicKey, PrivateKey privateKey, ZonedDateTime creationDate, byte[] address, BigDecimal balance, byte[] walletHash) throws NoSuchAlgorithmException {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.signature = Signature.getInstance(CryptoKeyGenerator.getSignatureAlgorithm());
        this.creationDate = creationDate;
        this.address = address;
        this.balance = balance;
        this.walletHash = walletHash;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public byte[] getAddress() {
        return address;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    protected Wallet updateBalance(BigDecimal delta, boolean isNegative) throws NoSuchAlgorithmException, NoSuchProviderException {
        BigDecimal newBalance;
        if(isNegative){
            newBalance = this.balance.subtract(delta);
        } else {
            newBalance = this.balance.add(delta);
        }
        return new Wallet(this.publicKey, this.privateKey, this.creationDate, this.address, newBalance, walletHash);
    }

    public void signTransaction(Transaction txn) throws InvalidKeyException, SignatureException {
        signature.initSign(privateKey);
        byte[] txnData = txn.getTxnHash();
        signature.update(txnData, 0, txnData.length);
        txn.setSignature(signature.sign());
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public byte[] getWalletHash() {
        return walletHash;
    }

    public String getHumanReadableAddress() {
        return CryptoHasher.humanReadableHash(address);
    }

    @Override
    public String toString() {
        return CryptoHasher.humanReadableHash(walletHash);
    }

    /**
     * Builder class is the Wallet builder. This is to ensure some level
     * of data protection by enforcing non-direct data access and immutable data.
     */
    public static class Builder {

        private PublicKey publicKey;
        private PrivateKey privateKey;
        private ZonedDateTime creationDate;
        private byte[] address;

        private Builder(){}

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder setPublicKey(PublicKey publicKey) {
            this.publicKey = publicKey;
            return this;
        }

        public Builder setKeyPair(KeyPair keyPair) {
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
            return this;
        }

        public Wallet build() throws NoSuchAlgorithmException, NoSuchProviderException {
            this.creationDate = DateTimeUtil.getCurrentTimestamp();
            this.address = publicKey.getEncoded();
            return new Wallet(this);
        }

        private byte[] buildWalletAddress(byte[] publicKeyEncoded) {
            byte[] address = new byte[20];
            int j = 0;
            for(int i = publicKeyEncoded.length-20; i < publicKeyEncoded.length; i++){
                address[j] = publicKeyEncoded[i];
                j++;
            }
            return address;
        }

        /*private byte[] signature() {
            privateKey.getEncoded();
        }*/
    }
}

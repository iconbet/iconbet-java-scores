package com.iconbet.score.ibpnp.db;

import score.Address;
import score.BranchDB;
import score.Context;
import score.VarDB;

import javax.swing.*;
import java.math.BigInteger;

public class WalletLinkData {
    public final BranchDB<String, VarDB<String>> requested_wallet = Context.newBranchDB("requested_wallet", String.class);
    public final BranchDB<String, VarDB<String>> request_status = Context.newBranchDB("request_status", String.class);
    public final BranchDB<String, VarDB<BigInteger>> requested_block = Context.newBranchDB("requested_block", BigInteger.class);
    public final BranchDB<String, VarDB<String>> wallet_type = Context.newBranchDB("wallet_type", String.class);


    public void setRequested_wallet(String walletLinkPrefix, String requestedWallet) {
        this.requested_wallet.at(walletLinkPrefix).set(requestedWallet.toString());
    }

    public void setRequest_status(String walletLinkPrefix, String requestStatus) {
        this.request_status.at(walletLinkPrefix).set(requestStatus);
    }

    public void setRequested_block(String walletLinkPrefix, BigInteger requestedBlock) {
        this.requested_block.at(walletLinkPrefix).set(requestedBlock);
    }

    public void setWallet_type(String walletLinkPrefix, String walletType) {
        this.wallet_type.at(walletLinkPrefix).set(walletType);
    }

    public String getRequested_wallet(String walletLinkPrefix) {
        return requested_wallet.at(walletLinkPrefix).getOrDefault("");
    }

    public String getRequest_status(String walletLinkPrefix) {
        return request_status.at(walletLinkPrefix).getOrDefault("");
    }

    public BigInteger getRequested_block(String walletLinkPrefix) {
        return requested_block.at(walletLinkPrefix).getOrDefault(BigInteger.ZERO);
    }

    public String getWallet_type(String walletLinkPrefix) {
        return wallet_type.at(walletLinkPrefix).getOrDefault("");
    }
}

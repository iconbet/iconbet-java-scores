package com.iconbet.score.ibpnp.db;

import score.Address;
import score.VarDB;
import score.Context;
import score.BranchDB;
import score.annotation.Optional;

import java.math.BigInteger;

public class UserData {
    private final BranchDB<String, VarDB<String>> username = Context.newBranchDB("username", String.class);
    private final BranchDB<String, VarDB<String>> wallet_address = Context.newBranchDB("user_address", String.class);
    private final BranchDB<String, VarDB<BigInteger>> token_id = Context.newBranchDB("token_id", BigInteger.class);

    private final BranchDB<String, VarDB<String>> game = Context.newBranchDB("game_name", String.class);
    private final BranchDB<String, VarDB<BigInteger>> amount_wagered = Context.newBranchDB("amount_wagered", BigInteger.class);
    private final BranchDB<String, VarDB<BigInteger>> amount_won = Context.newBranchDB("amount_won", BigInteger.class);
    private final BranchDB<String, VarDB<BigInteger>> amount_lost = Context.newBranchDB("amount_lost", BigInteger.class);
    private final BranchDB<String, VarDB<Integer>> bets_won = Context.newBranchDB("bets_won", Integer.class);
    private final BranchDB<String, VarDB<Integer>> bets_lost = Context.newBranchDB("bets_lost", Integer.class);

    private final BranchDB<String, VarDB<BigInteger>> largest_bet = Context.newBranchDB("largest_bet", BigInteger.class);
    private final BranchDB<String, VarDB<Integer>> wager_level = Context.newBranchDB("wager_level", Integer.class);
    private final BranchDB<String, VarDB<String>> linked_wallet = Context.newBranchDB("linked_wallet", String.class);
    private final BranchDB<String, VarDB<BigInteger>> lastAmountWagered = Context.newBranchDB("last_amount_wagered", BigInteger.class);

    public void setUsername(String proposalPrefix, String name) {
        this.username.at(proposalPrefix).set(name);
    }

    public void setWallet_address(String proposalPrefix, Address walletAddress) {
        this.wallet_address.at(proposalPrefix).set(walletAddress.toString());
    }

    public void setToken_id(String proposalPrefix, BigInteger tokenID) {
        this.token_id.at(proposalPrefix).set(tokenID);
    }

    public void setGame(String proposalPrefix, String game) {
        this.game.at(proposalPrefix).set(game);
    }

    public void setAmount_wagered(String proposalPrefix, BigInteger amountWagered) {
        this.amount_wagered.at(proposalPrefix).set(amountWagered);
    }

    public void setAmount_won(String proposalPrefix, BigInteger amountWon) {
        this.amount_won.at(proposalPrefix).set(amountWon);
    }

    public void setAmount_lost(String proposalPrefix, BigInteger amountLost) {
        this.amount_lost.at(proposalPrefix).set(amountLost);
    }

    public void setBets_won(String proposalPrefix, int betsWon) {
        this.bets_won.at(proposalPrefix).set(betsWon);
    }

    public void setBets_lost(String proposalPrefix, int betsLost) {
        this.bets_lost.at(proposalPrefix).set(betsLost);
    }

    public void setLargest_bet(String proposalPrefix, BigInteger largestBet) {
        this.largest_bet.at(proposalPrefix).set(largestBet);
    }

    public void setWager_level(String proposalPrefix, int wagerLevel) {
        this.wager_level.at(proposalPrefix).set(wagerLevel);
    }

    public void setLinked_wallet(String proposalPrefix, String linkedWallet) {
        this.linked_wallet.at(proposalPrefix).set(linkedWallet);
    }

    public void setLastAmountWagered(String proposalPrefix, BigInteger lastAmountWagered){
        this.lastAmountWagered.at(proposalPrefix).set(lastAmountWagered);
    }

    public String getUsername(String proposalPrefix) {
        return username.at(proposalPrefix).getOrDefault("");
    }

    public String getWallet_address(String proposalPrefix) {
        return wallet_address.at(proposalPrefix).getOrDefault("");
    }

    public BigInteger getToken_id(String proposalPrefix) {
        return token_id.at(proposalPrefix).getOrDefault(BigInteger.ZERO);
    }

    public String getGame(String proposalPrefix) {
        return game.at(proposalPrefix).getOrDefault("");
    }

    public BigInteger getAmount_wagered(String proposalPrefix) {
        return amount_wagered.at(proposalPrefix).getOrDefault(BigInteger.ZERO);
    }

    public BigInteger getAmount_won(String proposalPrefix) {
        return amount_won.at(proposalPrefix).getOrDefault(BigInteger.ZERO);
    }

    public BigInteger getAmount_lost(String proposalPrefix) {
        return amount_lost.at(proposalPrefix).getOrDefault(BigInteger.ZERO);
    }

    public int getBets_won(String proposalPrefix) {
        return bets_won.at(proposalPrefix).getOrDefault(0);
    }

    public int getBets_lost(String proposalPrefix) {
        return bets_lost.at(proposalPrefix).getOrDefault(0);
    }

    public BigInteger getLargest_bet(String proposalPrefix) {
        return largest_bet.at(proposalPrefix).getOrDefault(BigInteger.ZERO);
    }

    public int getWager_level(String proposalPrefix) {
        return wager_level.at(proposalPrefix).getOrDefault(0);
    }

    public String getLinked_wallet(String proposalPrefix) {
        return linked_wallet.at(proposalPrefix).getOrDefault("");
    }

    public BigInteger getLastAmountWagered(String proposalPrefix){
        return lastAmountWagered.at(proposalPrefix).getOrDefault(BigInteger.ZERO);
    }
}
